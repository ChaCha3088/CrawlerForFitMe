package com.diva.batch.job.musinsa;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import com.diva.batch.entity.fitme.Brand;
import com.diva.batch.entity.fitme.Category;
import com.diva.batch.entity.fitme.Gender;
import com.diva.batch.entity.fitme.Product;
import com.diva.batch.querydsl.itemreader.QuerydslPagingItemReader;
import io.github.bonigarcia.wdm.WebDriverManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import static com.diva.batch.entity.fitme.Gender.FEMALE;
import static com.diva.batch.entity.fitme.Gender.MALE;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MusinsaJobConfig {
    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AmazonS3 amazonS3;

    @Value("${AWS.BUCKET}")
    private String bucket;

    private static final int CHUNK_SIZE = 1;

    @Bean
    public Job MusinsaJob() {
        return new JobBuilder("musinsaJob", jobRepository)
                .start(musinsaStep1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step musinsaStep1(@Value("#{jobParameters[date]}") String date) {
        return (Step) new StepBuilder("musinsaStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> Musinsa Step1 Start");

                    // 텍스트 파일을 읽는다.
                    System.setIn(new FileInputStream("input.txt"));
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.setProperty("webdriver.chrome.driver", "C:\\Users\\SSAFY\\Downloads\\chromedriver-win64\\chromedriver.exe");
                    WebDriver driver = new ChromeDriver();
                    for (int i = 0; i < 17; i++) {
                        StringTokenizer st = new StringTokenizer(br.readLine());
                        String id = st.nextToken();
                        String name = st.nextToken();
                        // 페이지
                        for (int page = 1; page <= 1; page++) {
                            // 요청을 보낸다.
                            driver.get("https://www.musinsa.com/categories/item/"+id+"?d_cat_cd="+id+"&list_kind=small&sort=pop_category&page="+page+"&display_cnt=90&sort=sale_high&sub_sort=1m");
                            WebElement linkElement = driver.findElement(By.id("searchList"));
//                            URI url2= new URI("https://www.musinsa.com/categories/item/"+id+"?d_cat_cd="+id+"&list_kind=small&sort=pop_category&page="+page+"&display_cnt=90");

//                            System.out.println(url2);
//                            Desktop.getDesktop().browse(url2);
//                            final Connection.Response response = connection.execute();
//                            final Document doc = response.parse();
//                            ArrayList<WebElement> links = new ArrayList<>();
                            List<WebElement> links = linkElement.findElements(By.cssSelector("li > div.li_inner > div.list_img > a"));
                            List<String> linkUrl = new ArrayList<>();

                            for (int j = 0; j < links.size(); j++) {
                                linkUrl.add(links.get(j).getAttribute("href"));
                            }

                            for (int item = 0; item <1; item++) {
                                //상품 상세 페이지
                                driver.get(linkUrl.get(item));
                                // 브랜드
                                String brand = driver.findElement(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > div.product-detail__sc-achptn-0.bHXxTQ > ul > li:nth-child(1) > div.product-detail__sc-achptn-6.TehCn > a")).getText();
                                System.out.println("brand: "+brand);
                                // 카테고리
                                String category = driver.findElement(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-2.bIfCpH > div.product-detail__sc-up77yl-0.KuCLD > a:nth-child(3)")).getText();
                                System.out.println("category: "+ category);

                                //태그
                                List<WebElement> tags = driver.findElements(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > div.product-detail__sc-achptn-0.bHXxTQ > div > a"));
                                List<String> trueTags = new ArrayList<>();
                                for (int j = 0; j < tags.size(); j++) {
                                    trueTags.add(tags.get(j).getText());
                                    System.out.println("tag: "+trueTags.get(j));
                                }
                                // 이름
                                String productName = driver.findElement(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-2.bIfCpH > div.product-detail__sc-1klhlce-0.dxKrpT h3")).getText();
                                System.out.println("name: " + productName);

                                //성별
                                String gender =driver.findElement(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > div.product-detail__sc-achptn-0.bHXxTQ > ul > li:nth-child(2) > div.product-detail__sc-achptn-6.TehCn > span:last-child")).getText();
                                int g;
                                if(gender.equals("남성")){
                                    g=0;
                                } else if (gender.equals("여성")){
                                    g=1;
                                } else{
                                    g=2;
                                }
                                System.out.println("gender: " + gender);
//

                                String ageRange = driver.findElement(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > button > strong")).getText();
                                System.out.println("agerange: "+ageRange);

                                String price = driver.findElement(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > div.product-detail__sc-w5wkld-0.hgCYZm > div.product-detail__sc-1p1ulhg-0.jEclp > ul > li:nth-child(1) > div.product-detail__sc-1p1ulhg-6.fKNtEN > span")).getText();
                                System.out.println("price: "+price);


                                // 이미지들
                                List<WebElement> imagetags = driver.findElements(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-3.jKqPJk > div.product-detail__sc-p62agb-0.daKJsk > ul > li > img"));
                                List<String> images = new ArrayList<>();

                                for (int j = 0; j < imagetags.size(); j++) {
                                    images.add(imagetags.get(j).getAttribute("src"));
                                    System.out.println("images: "+images.get(j));
                                }


//                                for (Element imageElement : imageElements) {
//                                    String imageUrl = imageElement.absUrl("src");
//                                    //다ㅜㅇㄴ로드
//                                    URL url = new URL(imageUrl);
//                                    HttpURLConnection imageConnection = (HttpURLConnection) url.openConnection();
//                                    imageConnection.setRequestMethod("GET");
//                                    InputStream in = imageConnection.getInputStream();
//                                    //s3 업로드
//                                    byte[] imageData = IOUtils.toByteArray(in);
//                                    ObjectMetadata metadata = new ObjectMetadata();
//                                    metadata.setContentType(imageConnection.getContentType());
//                                    metadata.setContentLength(imageData.length);
//
//
//
//                                    in.close();
//                                    imageConnection.disconnect();
//                                }
//                                // 상세 이미지 리스트 #root > div:nth-child(3) > section.product-detail__sc-5zi22l-0.dqNYU > div > div.product-detail__sc-5zi22l-2.bPWBJM > div > div.product-detail__sc-5zi22l-4.etZXLC > div.product-detail__sc-5zi22l-5.gtnptH
                                List<WebElement> detailImagetags = driver.findElements(By.cssSelector("#root > div:nth-child(3) > section.product-detail__sc-5zi22l-0.dqNYU > div > div.product-detail__sc-5zi22l-2 > div > div > div> img"));
                                List<String> detailImages = new ArrayList<>();
                                for (int j = 0; j < detailImagetags.size(); j++) {
                                    detailImages.add(detailImagetags.get(j).getAttribute("src"));
                                    System.out.println("detailimages: "+detailImages.get(j));
                                }
//                                Elements image2Elements = document.select("#root > div:nth-child(3) > section.product-detail__sc-5zi22l-0.dqNYU > div > div.product-detail__sc-5zi22l-2.bPWBJM > div > div.product-detail__sc-5zi22l-4.etZXLC > div.product-detail__sc-5zi22l-5.gtnptH");
//                                for (Element imageElement : imageElements) {
//                                    String imageUrl = imageElement.absUrl("src");
//                                    URL url = new URL(imageUrl);
//                                    HttpURLConnection imageConnection = (HttpURLConnection) url.openConnection();
//                                    imageConnection.setRequestMethod("GET");
//                                    InputStream in = imageConnection.getInputStream();
//                                    //s3 업로드
//                                    byte[] imageData = IOUtils.toByteArray(in);
//                                    ObjectMetadata metadata = new ObjectMetadata();
//                                    metadata.setContentType(imageConnection.getContentType());
//                                    metadata.setContentLength(imageData.length);
                                // 색상 옵션 리스트
                                List<WebElement> colorOptionTags = driver.findElements(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > div.product-detail__sc-1rv3k2r-0.hvpCPl > div.product-detail__sc-1d13nsy-0.kngwRA > select:nth-child(1) > option"));
                                System.out.println(colorOptionTags.size());
                                List<String> colorOptions = new ArrayList<>();
                                for (int j = 0; j < colorOptionTags.size()-1; j++) {
                                    colorOptions.add(colorOptionTags.get(j+1).getText());
                                    System.out.println("coloroption: "+colorOptions.get(j));
                                }

                                // 사이즈 옵션 리슨트
                                List<WebElement> sizeOptionTags = driver.findElements(By.cssSelector("#root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-3.jKqPJk > div.product-detail__sc-swak4b-0.KLfjI > table > tbody > tr > th"));
                                System.out.println(sizeOptionTags.size());
                                List<String> sizeOptions = new ArrayList<>();
                                for (int j = 0; j < sizeOptionTags.size()-1; j++) {
                                    sizeOptions.add(sizeOptionTags.get(j+1).getText());
                                    System.out.println("sizeoption: "+sizeOptions.get(j));
                                }
//                                #root > div.product-detail__sc-8631sn-0.gJskhq > div.product-detail__sc-8631sn-1.fPAiGD > div.product-detail__sc-8631sn-4.goIKhx > div.product-detail__sc-1rv3k2r-0.hvpCPl > div.product-detail__sc-1d13nsy-0.kngwRA > select:nth-child(1) > option:nth-child(1)
//
//                                    in.close();
//                                    imageConnection.disconnect();
//                                }
                            }
                        }


                    }
                    driver.quit();
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }

    @Bean
    public QuerydslPagingItemReader<Product> musinsaReader() {
        return null;
    }
}
