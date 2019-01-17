import org.junit.*;
import org.junit.rules.ErrorCollector;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.yandex.qatools.allure.annotations.Severity;
import ru.yandex.qatools.allure.annotations.Step;
import ru.yandex.qatools.allure.model.SeverityLevel;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

public class TestStackoverflow {
    private static RemoteWebDriver driver;
    private HashMap<String, String> questionTitlesAndLinksMap = new HashMap<>();

    @BeforeClass
    public static void setUpBeforeClass() throws MalformedURLException {
        System.out.println("Начинаю тест \"Тестовое задание на позицию АТ\"");
        URL chromeDriverUrl = new URL("http://localhost:9515");

        //  Открыть браузер Chrome и развернуть на весь экран.
        System.out.print("Загружаю драйвер...");
        driver = new RemoteWebDriver(chromeDriverUrl, new ChromeOptions()); // открываем хром
        System.out.println("Загружено");
        System.out.println();
        driver.manage().window().maximize(); // разворачиваем окно
        driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS); // устанавливаем время ожидания прогрузки страницы
    }

    @Before
    public void setUp() {
        System.out.println("\t Начинаю новый тест");

        // 1) Перейти на внешний ресурс: http://stackoverflow.com/
        System.out.println("1) Перейти на внешний ресурс: http://stackoverflow.com/ ");
        driver.get("http://stackoverflow.com/");

    }

    @AfterClass
    public static void tearDown() {
        try {
            System.out.print("Закрываю драйвер...");
            driver.close();
            System.out.println("Закрыто");
        } catch (NullPointerException e) {
            System.out.println("Не удалось закрыть драйвер:" + e.getMessage());
        }
        System.out.println("Завершаю тест \"Тестовое задание на позицию АТ\"");
    }

    @Test
    public void testWebdriverStackOverflow() throws InterruptedException {

        // 2) В строку поиска ввести значение «webdriver».
        searchWebDriver();

        // 3) Проверить, что в каждом результате представлено слово WebDriver.
        parseAndCheckSearchResults();

        // 4) Войти в каждое обсуждения из выборки и убедиться, что перешли именно в эту тему (проверить заголовок обсуждения).
        gotoEveryQuestionTestTitle();

        // 5) Перейти в раздел Tags
        gotoTags();

        // 6) В строку поиска ввести значение – webdriver. Убедиться, что в результате присутствуют элементы содержащее слово webdriver.
        searchWebDriverTagAndCheck();

        // 7) Найти в результатах тэг по точному совпадению поискового запроса и кликнуть по нему, проверить, что после перехода отображаются обсуждения помеченные тэгом webdriver.
        clickWebDriverTagAndCheck();
    }

    @Step("2) В строку поиска ввести значение «webdriver».")
    private void searchWebDriver() {
        System.out.println("2) В строку поиска ввести значение «webdriver».");
        driver.findElement(By.xpath("//input[contains(@name, 'q')]")).sendKeys("webdriver");
        driver.findElement(By.xpath("//button[contains(@class, 'js-search-submit')]")).click();
    }


    @Step("3) Проверить, что в каждом результате представлено слово WebDriver.")
    private void parseAndCheckSearchResults() {
        System.out.println("3) Проверить, что в каждом результате представлено слово WebDriver.");

        // сначала получим массив всех ссылок (в которых есть название) на вопросы, которые находятся на странице
        List<WebElement> questionLinksList = driver.findElements(By.xpath("//a[@class = 'question-hyperlink']"));

        // поскольку в дальнейшем мы будем работать только с заголовками и ссылками, то можно распарсить WebElements questionLinksList
        for (WebElement questionLink : questionLinksList) {
//            String title = questionLink.getAttribute("Title");
            String title = questionLink.getText(); // если использовать строку которая выше то не захватиться [closed] из заголовка
            title = title.substring(2).trim(); // убираем "Q:" из текста в начале ссылки и подрезаем пробелы в начале и конце
            String href = questionLink.getAttribute("href");
            questionTitlesAndLinksMap.put(title, href);
        }

        // а массив WebElement'ов можно удалить чтобы не занимал память
        questionLinksList.clear();

        //полным перепобром проверим что в каждой ссылке есть слово webdriver
        for (String questionTitle : questionTitlesAndLinksMap.keySet()) {

            if (!questionTitle.toLowerCase().contains("webdriver")) {
                System.out.println("\tСсылка не содержит слово WebDriver: \n\t\"" + questionTitle + "\"");
            }
            // проверяем содержит ли текст ссылки слово webdriver. всё в нижнем регистре чтобы было caseInsensitive
            assertTrue(
                    "Ссылка не содержит слово WebDriver: \n\t\"" + questionTitle + "\""
                    , questionTitle.toLowerCase().contains("webdriver")
            );
        }
    }

    @Step("4) Войти в каждое обсуждения из выборки и убедиться, что перешли именно в эту тему (проверить заголовок обсуждения).")
    private void gotoEveryQuestionTestTitle() {
        System.out.println("4) Войти в каждое обсуждения из выборки и убедиться, что перешли именно в эту тему (проверить заголовок обсуждения).");
        // у нас уже есть массив ссылок на вопросы поэтому просто делаем его ещё один его перебор
        for (Map.Entry<String, String> entry : questionTitlesAndLinksMap.entrySet()) {
            String expectedTitle = entry.getKey().trim(); // получаем ключ записи, который является текстом заголовка вопроса из поиска
            String link = entry.getValue();// значемнием записи является собственно ссылка вида "/questions/54239799/issues-with-auto-login-using-request"

            driver.get(link); // переходим по ссылке в обсуждение

            // заголовок на странице обсуждения представляет собой ссылку <a> обернутую в тег <h1> поэтому сразу ищем ссылку
            String actualTitle = driver.findElement(By.xpath("//h1[contains(@class, 'fs-headline')]/a[contains(@class, 'question-hyperlink')]")).getText();


            // строки вроде равны, но программа считает что не равны
            // Проблема была в невидимых лишних пробелах, но всеравно оставил приведение ко всем только видимым символам
            // https://stackoverflow.com/questions/13966302/string-equals-not-working-for-me
            // https://stackoverflow.com/questions/6198986/how-can-i-replace-non-printable-unicode-characters-in-java
//            expectedTitle = expectedTitle.replaceAll("[\\p{Z}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", " ");
//            actualTitle = actualTitle.replaceAll("[\\p{Z}\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", " ");
// TODO удалить всё что выше т.к. assertEquals видимо работает подругому чем просто String.equals()

//            if (expectedTitle.equals(actualTitle)) {
//                System.out.println("\tExpected\t:" + expectedTitle + "\n\tActual\t\t:" + actualTitle);
//            }

            // бывает Expected:Headless Browser and scraping - solutions ; Actual:Headless Browser and scraping - solutions [closed]
            Assert.assertEquals("Заголовок должен соответствовать теме в которую заходим", expectedTitle, actualTitle);


        }
    }

    @Step("5) Перейти в раздел Tags")
    private void gotoTags() {
        System.out.println("5) Перейти в раздел Tags");
        driver.findElement(By.id("nav-tags")).click();
    }

    @Step("6) В строку поиска ввести значение – webdriver. Убедиться, что в результате присутствуют элементы содержащее слово webdriver.")
    private void searchWebDriverTagAndCheck() throws InterruptedException {
        System.out.print("6) В строку поиска ввести значение – webdriver. ");
        driver.findElement(By.id("tagfilter")).sendKeys("webdriver");
        Thread.sleep(5000); // ждем пока страница обновится
        System.out.println("Убедиться, что в результате присутствуют элементы содержащее слово webdriver.");
        List<WebElement> tagLinks = driver.findElements(By.xpath("//a[contains(@rel, 'tag')]"));
        boolean isWebDriverInTags = false; // переменная показывает что в результате присутствуют элементы содержащие слово webdriver
        int i = 0; // индекс по которому перебираем tagLinks в следующем цикле
        while ((!isWebDriverInTags) && (i < tagLinks.size())) { // ищем пока не найдем или не кончится массив
            WebElement tagLink = tagLinks.get(i);
            String tagText = tagLink.getText().toLowerCase();
            if (tagText.contains("webdriver")) {
                isWebDriverInTags = true; // если нашли webDriver в tagLinks то эта переменная говорит что можно дальше не искать
            }
            i++;
        }
        assertTrue("В результатах поиска по тегу должен присутствовать элемент содержащий слово webdriver", isWebDriverInTags);
    }

    @Step("7) Найти в результатах тэг по точному совпадению поискового запроса и кликнуть по нему, проверить, что после перехода отображаются обсуждения помеченные тэгом webdriver.")
    private void clickWebDriverTagAndCheck() {
        System.out.print("7) Найти в результатах тэг по точному совпадению поискового запроса и кликнуть по нему, ");
        driver.findElement(By.linkText("webdriver")).click(); // щелкаем на тэг по точному запросу

        System.out.println("проверить, что после перехода отображаются обсуждения помеченные тэгом webdriver");
        // можно было бы по заголовку Questions tagged [webdriver], но это слишком просто и не надежно
        // поэтому я возьму все теги с первой страницы из каждого вопроса и проверю что под каждым обсуждением есть тег webdriver
        // сначала получим все блоки с вопросами
        List<WebElement> questionsDivList = driver.findElements(By.className("question-summary"));
        // в каждом вопросе есть теги
        for (WebElement question : questionsDivList) {
            // выберем теги из под вопроса
            List<WebElement> tags = question.findElements(By.className("post-tag"));
            // проверим что в тегах содержится webdriver
            boolean isWebDriverInTags = false;
            for (WebElement tag : tags) {
                String tagText = tag.getText();
                if (tagText.equals("webdriver")) {
                    isWebDriverInTags = true;
                }
            }
            // если мы прошли все теги то должны были найти там webdriver
            assertTrue("В результатах поиска по тегу должены быть обсуждения помеченные тегом webdriver", isWebDriverInTags);
        }
    }
}
