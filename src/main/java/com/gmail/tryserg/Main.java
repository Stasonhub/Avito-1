package com.gmail.tryserg;


import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

public class Main {
    private static String fileName = stringDate();

    private static com.gmail.tryserg.Sender sslSender = new com.gmail.tryserg.Sender("EMAIL", "PASSWORD");

    public static void main(String[] args) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName + ".xml", true), "UTF-8");
        writer.write("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n\n");
        writer.write("<listings>\n");
        writer.flush();

        Document html = null;
        ArrayList linkArray = new ArrayList();
        ArrayList cityArray = new ArrayList();

        try {
            html = Jsoup.connect(readFromFile())
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36")
                    .timeout(2600000).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Elements links = html.getElementsByClass("description-title-link").select("a");
        for (Element link : links) {
            linkArray.add(link.attr("href"));
        }
        Elements city = html.getElementsByClass("data-chunk");
        for (Element element : city) {
            cityArray.add(element.text());
        }

        for (int i = 0; i < linkArray.size(); i++) {
            Elements elements;
            String title = "";
            String content = "";
            String category = "";
            String sellerName = "";
            String price = "";
            String sellerPhone = "";
            String address = "";
            String date = "";
            String image1 = "";
            String image2 = "";
            String image3 = "";
            String image4 = "";
            ArrayList imagesArray = new ArrayList();
            Document doc = null;

            java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
            WebClient webClient = new WebClient(BrowserVersion.FIREFOX_38);
            webClient.getOptions().setTimeout(20000);
            webClient.setAjaxController(new NicelyResynchronizingAjaxController());
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.setJavaScriptTimeout(20000);
            webClient.getOptions().setJavaScriptEnabled(true);

            HtmlPage page = webClient.getPage("https://m.avito.ru" + linkArray.get(i));
//            HtmlPage page = webClient.getPage("https://m.avito.ru/orsk/gruzoviki_i_spetstehnika/prodam_avtogreyder_793384615");
            List<HtmlAnchor> anchors1 = page.getAnchors();
            HtmlAnchor link2 = null;
            for (HtmlAnchor anchor : anchors1) {
                if (anchor.asText().indexOf("Показать номер") > -1) {
                    link2 = anchor;
                    break;
                }
            }
//            if (!link2.click().isHtmlPage()) {
//                continue;
//            }else page = link2.click();


            try {
                page = link2.click();
                doc = Jsoup.parse(page.asXml());
                title = doc.getElementsByAttributeValue("property", "og:title").attr("content");
                content = doc.getElementsByClass("description-preview-wrapper").html().replaceAll("<p>", "").replaceAll("</p>", "").trim();
//            elements = doc.getElementsByClass("param");
//            if (!elements.isEmpty()) {
                category = doc.getElementsByClass("param").first().text();
//            }
                sellerName = doc.getElementsByClass("person-name").last().text().replaceAll("\\(компания\\)", "").replaceAll("\\(контактное лицо\\)", "").trim().replaceAll("авито", "без имени");
                price = doc.getElementsByClass("price-value").text().replaceAll(" ", "").replaceAll("руб.", "").replaceAll("неуказана", "");
//                sellerPhone = doc.getElementsByClass("person-actions").first().text().replace("Написать", "").replaceAll(" ", "").replaceAll("-", "").trim();
                sellerPhone = doc.getElementsByClass("person-actions").select("a").attr("href").replaceAll("tel:", "").replaceAll(" ", "").replaceAll("-", "").trim();
                address = doc.getElementsByClass("avito-address-text").text();
                date = doc.getElementsByAttributeValue("property", "article:modified_time").attr("content");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                Date result = null;
                try {
                    result = df.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                date = dateFormat.format(result);
                elements = doc.getElementsByAttributeValue("property", "og:image");
                for (Element elm : elements
                        ) {
                    imagesArray.add(elm.attr("content"));
                }
                if (imagesArray.size() >= 4) {
                    image1 = imagesArray.get(0).toString();
                    image2 = imagesArray.get(1).toString();
                    image3 = imagesArray.get(2).toString();
                    image4 = imagesArray.get(3).toString();
                }

                if (imagesArray.size() == 3) {
                    image1 = imagesArray.get(0).toString();
                    image2 = imagesArray.get(1).toString();
                    image3 = imagesArray.get(2).toString();
                }

                if (imagesArray.size() == 2) {
                    image1 = imagesArray.get(0).toString();
                    image2 = imagesArray.get(1).toString();
                }

                if (imagesArray.size() == 1) {
                    image1 = imagesArray.get(0).toString();
                }

                if (imagesArray.get(0).toString().contains("avito.png")) {
                    image1 = "";
                }


                System.out.println("===============");
                System.out.println(title);
                System.out.println(content);
                System.out.println(changeCat(category));
                System.out.println(sellerName);
                System.out.println(sellerPhone);
                System.out.println(price);
                System.out.println(address);
                System.out.println(date);
                System.out.println(cityArray.get(i));
                for (Object img : imagesArray
                        ) {
                    System.out.println(img);
                }
                System.out.println("===============");

                if (!title.isEmpty() && !category.isEmpty() && !sellerName.isEmpty() && !image1.isEmpty()) {
                    toFile(title, content, changeCat(category), sellerName, image1, image2, image3, image4, price, sellerPhone, "Россия", cityArray.get(i).toString(), date);
                }

                System.out.println("start pause");
                try {
                    Thread.sleep(50000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("end pause");


            } catch (NullPointerException ex) {
                ex.printStackTrace();
            } finally {
                continue;
            }

        }
        writer.write("</listings>\n\n");
        writer.flush();
        writer.close();
        System.out.println("========= FILE SAVED! ==========");
        sslSender.send("Объявления спарсено! " + fileName, "Объявление:<br>http://46.47.235.138/avito/" + fileName + ".xml<br>http://46.47.235.138/avito/", "strevi.com@gmail.com", "vyshnivskyi91@gmail.com");
        sslSender.send("Объявления спарсено! " + fileName, "Объявление:<br>http://46.47.235.138/avito/" + fileName + ".xml<br>http://46.47.235.138/avito/", "strevi.com@gmail.com", "tryserg@gmail.com");

    }


    public static String readFromFile() {
        String s = "";
        try {
            BufferedReader reader = new BufferedReader(new java.io.FileReader("link.txt"));
            s = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String changeCat(String category) {
        switch (category) {
            case "Автокраны":
                category = "Подъёмные Краны";
                break;
            case "Бульдозеры":
                category = "Бульдозеры и трубоукладчики";
                break;
            case "Лёгкий транспорт":
                category = "Легковой автомобиль";
                break;
//            case "Прицепы":
//                category = "-------------";
//                break;
            case "Техника для лесозаготовки":
                category = "Лесотехника";
                break;
            case "Тягачи":
                category = "Седельный тягач";
                break;
            case "Автобусы":
                category = "Пассажирские автобусы";
                break;
        }

        return category;
    }

    public static String stringDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd-HHmm");
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static void toFile(String title, String content, String category, String contactname,
                              String image1, String image2, String image3, String image4, String price, String sellerphone, String country,
                              String city, String date) {

        try {

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName + ".xml", true), "UTF-8");
            writer.write("<listing>\n");
            if (!title.isEmpty()) {
                writer.write("<title><![CDATA[" + title + "]]></title>\n");
            }
            if (!content.isEmpty() && content.length() > 10) {
                writer.write("<content><![CDATA[" + content + "]]></content>\n");
            } else {
                writer.write("<content><![CDATA[" + "Купить " + category.toLowerCase() + " " + title.toLowerCase() + "]]></content>\n");
            }
            if (!category.isEmpty()) {
                writer.write("<category lang=\"ru_RU\">" + category + "</category>\n");
            }
            writer.write("<contactemail>" + "ads@strevi.com" + "</contactemail>\n");
            if (!contactname.isEmpty()) {
                writer.write("<contactname>" + contactname + "</contactname>\n");
            }
            if (!image1.isEmpty()) {
                writer.write("<image>" + image1 + "</image>\n");
            }
            if (!image2.isEmpty()) {
                writer.write("<image>" + image2 + "</image>\n");
            }
            if (!image3.isEmpty()) {
                writer.write("<image>" + image3 + "</image>\n");
            }
            if (!image4.isEmpty()) {
                writer.write("<image>" + image4 + "</image>\n");
            }
            if (!price.isEmpty()) {
                writer.write("<price>" + price + "</price>\n");
            }

            writer.write("<countryId>" + "RU" + "</countryId>\n");
            writer.write("<currency>" + "RUB" + "</currency>\n");

            if (!sellerphone.isEmpty()) {
                writer.write("<custom name=\"sellerphone\">" + sellerphone + "</custom>\n");
            }
            if (!country.isEmpty()) {
                writer.write("<country>" + country + "</country>\n");
            }

            if (!city.isEmpty()) {
                writer.write("<city>" + city + "</city>\n");
            }

            if (!date.isEmpty()) {
                writer.write("<datetime>" + date + "</datetime>\n");
            }
            writer.write("</listing>\n");

            writer.flush();
            writer.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
    }


//    public static ArrayList linksArray() {
//        Document doc = null;
//        ArrayList linkArray = new ArrayList();
//
//        try {
//            doc = Jsoup.connect(readFromFile())
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36")
//                    .timeout(2600000).get();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Elements links = doc.getElementsByClass("description-title-link").select("a");
//        for (Element link : links) {
//            linkArray.add(link.attr("href"));
//        }
//        return linkArray;
//
//
//    }


}




