package ru.xou.task03_url;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.io.*;
import com.google.gson.*;

public class Parser {
    public static void main(String[] args) throws Exception {
        String pathToWorkingDirectory;
        String line;//строка с json-представлением до форматирования
        String pathToSavingFile;
        boolean isFound = false;//найден ли актер
        final int MILLISECONDSINDAY = 1000 * 3600 * 24;
        String page = null;
        StringBuilder pageBuilder = new StringBuilder();

        Scanner input = new Scanner(System.in);         //ввод
        System.out.print("Введите имя: ");
        String firstName = input.next().toLowerCase();
        System.out.print("Введите фамилию: ");
        String secName = input.next().toLowerCase();
        String path = String.format("http://www.theimdbapi.org/api/find/person?name=%s+%s", firstName, secName); //URL

        File file = new File(pathToSavingFile = String.format(pageToWorkingDirectory + "%s%s", firstName, secName));

        //если файла нет или он не обновлялся больше дня - подключается к БД
        if (!file.exists() || ((System.currentTimeMillis() - file.lastModified()) >  MILLISECONDSINDAY)) {
            file.createNewFile(); //создание файла и получение страница
            System.out.println("Подключаемся");
            //инициализация подключения
            URL url = new URL(path);
            URLConnection uc = url.openConnection();
            uc.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)");
            uc.addRequestProperty("Content-Type", "text/html; charset=utf-8");
            uc.connect();
            //организуем поток для чтения страницы из сети
            InputStream is = uc.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(is, "CP1251");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            //в pageBuilder добавляем одной строкой содержание страницы
            while ((line = bufferedReader.readLine()) != null) {
                pageBuilder.append(line);
            }
            //запись в файл
            try(BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(pathToSavingFile))){
                //когда актера не существует
                if(pageBuilder.toString().equals("null")){
                    //System.out.println("Актер не найден");
                    file.delete();
                    isFound = false;
                    bufferedWriter.close();
                }
                //в другом случае
                else {
                    //приводим в приличный вид строку
                    page = pageBuilder.toString().substring(3, pageBuilder.length() - 1).replaceAll("[\\s]{2,}", "");
                    bufferedWriter.write(page);
                    bufferedWriter.close();
                    isFound = true;
                }
            }
            catch (Exception ex){
                System.out.println(ex.getMessage());
            }
        } else { //если есть в кэше
            System.out.println("Используем кэш");
            //читаем содержание файла
            try (BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToSavingFile))) {
                while ((line = bufferedReader.readLine()) != null) {
                    page = line;
                }
                bufferedReader.close();
                isFound = true;
            }
            catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
        }

        if (isFound){
            //System.out.println(page);
            //String jsonLine = pageBuilder.toString().substring(3, pageBuilder.length() - 1).replaceAll("[\\s]{2,}", "");
            GsonBuilder builder = new GsonBuilder();
            builder.setPrettyPrinting().serializeNulls();
            Gson gson = builder.create();
            System.out.println();
            Person person = gson.fromJson(page, Person.class);
            System.out.printf("Имя актера: %s\n", person.title);
            System.out.printf("Дата рождения: %s\n", person.birthday);
            Filmography filmography = person.filmography;
            System.out.println("Фильмы:");
            List<Actor> actor = filmography.actor;
            for (Actor a : actor ){
                System.out.printf("\t%s\n", a.title);
            }
        }
        else System.out.println("Актер не найден");
    }
}

class Person{
    public String title;
    public String birthday;
    Filmography filmography = new Filmography();
}

class Filmography {
    List<Actor> actor = new ArrayList<>();
}

abstract class PersonType{
   // public String url;
   // public String year;
   // public String type;
   // public String imdb_id;
   // public String title;
}

class Actor extends PersonType {

}
