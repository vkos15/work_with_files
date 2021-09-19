package ru.vkost;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import net.lingala.zip4j.ZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class SelenideFileTest {

    @Test
        //скачивание файла
    void downloadFileTest() throws Exception {
        open("https://github.com/selenide/selenide/blob/master/README.md");
        File download = $("#raw-url").download();
        String result;
        try (InputStream is = new FileInputStream(download)) {
            result = new String(is.readAllBytes(), "UTF-8");
        }
        assertThat(result).contains("Selenide");
    }

    @Test
        //работа с текстовым файлом
    void TestTextFile() throws Exception {
        String result;

        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("Test1.txt")) {
            result = new String(stream.readAllBytes(), "UTF-8");
        }
        assertThat(result).contains("Ночь, улица, фонарь, аптека");
    }

    @Test
        //работа с pdf файлом
    void TestPDFFile() throws Exception {
        PDF result;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("Test2.pdf")) {
            result = new PDF(stream);
        }
        assertThat(result.text).contains("Бессмысленный и тусклый свет.");
    }


    @Test
        //работа с xls файлом
    void TestXLSFile() throws Exception {
        XLS result;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("Test3.xlsx")) {
            result = new XLS(stream);
        }
        assertThat(result.excel.getSheet("Соревнования").getRow(2)
                .getCell(0).getStringCellValue()).contains("Кубок Главы Удмуртии");
    }

    @Test
        //работа с zip-файлом без пароля, проверка имени файла в архиве
    void TestZipFile() throws Exception {
        ZipInputStream result;
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("Test5.zip")) {
            result = new ZipInputStream(stream);
            ZipEntry entry = result.getNextEntry();
            assertThat(entry.getName()).contains("sfsdf.txt");
        }
    }

    @Test
        //работа с zip-файлом с паролем, распаковали архив и проверили содержимое файла
    void TestZipFileWithPassword() throws Exception {
        ZipFile zipFile = new ZipFile("./src/test/resources/Test4.zip");
        if (zipFile.isEncrypted())
            zipFile.setPassword("qwerty".toCharArray());
        //Проверили имя файла в архиве
        assertThat(zipFile.getFileHeaders().get(0).toString()).contains("URA.txt");
        //Извлекли файл
        zipFile.extractAll("./src/test/resources/extractzip");

        String result;
        try (FileInputStream stream = new FileInputStream("./src/test/resources/extractzip/URA.txt")) {
            result = new String(stream.readAllBytes(), "UTF-8");
        }
        //проверили, что в файле есть заданный текст
        assertThat(result).contains("Хрупкий снег изломан весь.");
    }


    @Test
        //работа с docx, библиотека docx4j https://docx4java.org/docx4j/Docx4j_GettingStarted.pdf
    void TestDocsFile() throws Exception {
        WordprocessingMLPackage wordMLPackage =
                WordprocessingMLPackage.load(new java.io.File("./src/test/resources/Test6.docx"));
        String text = wordMLPackage.getMainDocumentPart().getContent().toString();
        assertThat(text).contains("Избушка там на курьих ножках");
    }

}