package com.diva.batch.job.musinsa;

import static com.diva.batch.entity.fitme.Gender.fromValue;

import com.diva.batch.entity.fitme.*;
import com.diva.batch.repository.*;
import com.diva.batch.utils.RandomNumber;
import jakarta.persistence.EntityManagerFactory;

import java.io.*;
import java.net.URL;
import java.time.Duration;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MusinsaJobConfig {
    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final TagRepository tagRepository;
    private final ProductTagRepository productTagRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ProductRepository productRepository;
    private final ProductColorRepository productColorRepository;

    @Bean
    public Job MusinsaJob() {
        return new JobBuilder("musinsaJob", jobRepository)
                .start(musinsaStep1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step musinsaStep1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("musinsaStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> Musinsa Step1 Start");

                    StringBuffer sb = new StringBuffer();

                    // 텍스트 파일을 읽는다.
                    System.setIn(new FileInputStream("input.txt"));
                    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                    System.setProperty("webdriver.chrome.driver", "C:\\Users\\SSAFY\\Downloads\\chromedriver-win64\\chromedriver.exe");
                    WebDriver driver = new ChromeDriver();
                    for (int i = 0; i < 17; i++) { // ToDo: 나중에 17로 변경할거야
                        StringTokenizer st = new StringTokenizer(br.readLine());
                        String categoryId = st.nextToken();
                        String categoryName = st.nextToken();

                        // 카테고리 생성
                        Category category = Category.builder()
                                .id(Long.parseLong(categoryId))
                                .name(categoryName)
                                .build();

                        categoryRepository.save(category);

                        // 페이지
                        for (int page = 5; page <= 5; page++) { // ToDo: 나중에 10으로 변경할거야
                            // 요청을 보낸다.
                            driver.get("https://www.musinsa.com/categories/item/"+ categoryId+"?d_cat_cd="+ categoryId+"&list_kind=small&sort=pop_category&page="+ page+"&display_cnt=90&sort=sale_high&sub_sort=1m");

                            // 검색된 상품들이 담긴 리스트
                            WebElement linkElement = driver.findElement(By.id("searchList"));
                            List<WebElement> links = linkElement.findElements(By.cssSelector("li > div.li_inner > div.list_img > a"));
                            // 검색된 상품들의 url만 담긴 리스트
                            List<String> linkUrl = new ArrayList<>();
                            // 리스트를 돌면서 url을 넣어준다.
                            for (WebElement link : links) {
                                linkUrl.add(link.getAttribute("href"));
                            }
                            Random random = new Random();
                            // 한 페이지에서 상품을 순회한다.
                            for (int item = 0; item < 90; item++) { // ToDo: 나중에 90으로 변경
                                // 1에서 2초 사이의 랜덤 딜레이를 생성
                                int delay = 500 + random.nextInt(500);

                                // 상품 상세 페이지 접근
                                driver.get(linkUrl.get(item));

                                System.out.println("######"+item+"화면 시작########");
                                Thread.sleep(delay);
                                // 브랜드
                                String brandName = "실패";
                                // 브랜드 없으면 생성
                                // 브랜드 조회

                                try {
                                    brandName = driver.findElement(By.cssSelector("#root > div > div > div > div > ul > li:nth-child(1) > div > a")).getText().trim();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                                Optional<Brand> brand = brandRepository.findByName(brandName);

                                Brand realBrand;

                                if (brand.isEmpty()) {
                                    Brand newBrand = Brand.builder()
                                            .name(brandName)
                                            .build();
                                    realBrand = brandRepository.save(newBrand);
                                }
                                else {
                                    realBrand = brand.get();
                                }

                                sb.append("brand:").append(brandName).append("\n");
                                System.out.println("######브랜드 끝########");
                                // 이름
                                String productName = "실패";
                                try {
                                    productName =  driver.findElement(By.cssSelector("#root > div > div > div> div> h3")).getText().trim();
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                                sb.append("productName: ").append(productName).append("\n");
                                System.out.println("######이름 끝########");
                                // 성별
                                String gender = "실패";
                                try {
                                    gender =  driver.findElement(By.cssSelector("#root > div> div > div > div> ul > li:nth-child(2) > div > span:last-child")).getText();
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                                int g;
                                if (gender.equals("남성")) {
                                    g = 0;
                                }
                                else if (gender.equals("여성")) {
                                    g = 1;
                                }
                                else{
                                    g = 2;
                                }
                                sb.append("gender: ").append(gender).append("\n");
                                System.out.println("######성별 ########");

                                // 나이대
                                String ageRange = "실패";
                                try {
                                    ageRange = driver.findElement(By.cssSelector("#root > div > div > div > button > strong")).getText().replace(",", "").replace("세 이상", "").trim();
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                                sb.append("ageRange: ").append(ageRange).append("\n");

                                // 가격
                                int price = 0;
                                try {
                                    price = Integer.parseInt(driver.findElement(By.cssSelector("#root > div > div > div > div > div > ul > li:nth-child(1) > div:nth-child(2) > span")).getText().replace(",", "").replace("원", ""));
                                } catch (Exception e){
                                    e.printStackTrace();
                                }

                                sb.append("price: ").append(price).append("\n");

                                // 상품 생성
                                Product newProduct = Product.builder()
                                    .brand(realBrand)
                                    .category(category)
                                    .name(productName)
                                    .gender(fromValue(g))
                                    .ageRange(ageRange)
                                    .price(price)
                                    .build();

                                newProduct = productRepository.save(newProduct);

                                Long id = newProduct.getId();
                                File Folder = new File("C:\\fitmeimg/"+id);
                                if (!Folder.exists()) {
                                    Folder.mkdirs(); // 폴더가 없으면 생성
                                }
                                File FolderMain = new File("C:\\fitmeimg/"+id+"/main");
                                if (!FolderMain.exists()) {
                                    FolderMain.mkdirs(); // 폴더가 없으면 생성
                                }
                                File FolderDetail = new File("C:\\fitmeimg/"+id+"/detail");
                                if (!FolderDetail.exists()) {
                                    FolderDetail.mkdirs(); // 폴더가 없으면 생성
                                }

                                // 메인 이미지
                                int mainImageCnt = 1;
                                List<WebElement> imageTags = driver.findElements(By.cssSelector("#root > div > div > div > div > ul > li > img"));
                                for (WebElement imageTag : imageTags) {
                                    String imageUrl = imageTag.getAttribute("src");
                                    String newImageUrl = imageUrl.replaceFirst("_60\\.", "_500.");
                                    try (InputStream in = new URL(newImageUrl).openStream();
                                         FileOutputStream  out = new FileOutputStream("C:\\fitmeimg\\" + id + "\\main\\" + "mainimage_" + id + "_" + mainImageCnt++ + ".jpg")){
                                        byte[] buffer = new byte[4096];
                                        int bytesRead;
                                        while ((bytesRead = in.read(buffer)) != -1) {
                                            out.write(buffer, 0, bytesRead);
                                        }
                                    }catch (Exception e ){
                                        e.printStackTrace();
                                    }

                                    MainImage.builder()
                                        .url(newImageUrl)
                                        .product(newProduct)
                                        .build();

                                    sb.append("imageUrl: ").append(newImageUrl).append("\n");

                                    // ToDo: 이미지 다운로드
                                    // product의 id로 폴더를 만들고 그 안에 이미지를 저장한다.
                                    // 이름은 prd_img/로
                                }


                                // 상세 이미지 리스트
                                int detailImageCnt = 1;
                                List<WebElement> detailImageTags = driver.findElements(By.cssSelector("#root > div:nth-child(3) > section > div > div > div > div > div img"));
                                for (WebElement detailImageTag : detailImageTags) {
                                    String detailImageUrl = detailImageTag.getAttribute("src");
                                    try (InputStream in = new URL(detailImageUrl).openStream();
                                         FileOutputStream  out = new FileOutputStream("C:\\fitmeimg\\" + id + "\\detail\\" + "detailimage" + id + "_" + detailImageCnt++ + ".jpg")){
                                        byte[] buffer = new byte[4096];
                                        int bytesRead;
                                        while ((bytesRead = in.read(buffer)) != -1) {
                                            out.write(buffer, 0, bytesRead);
                                        }
                                    }catch (Exception e ){
                                        e.printStackTrace();
                                    }
                                    System.out.println(detailImageUrl);
                                    DetailImage.builder()
                                        .url(detailImageUrl)
                                        .product(newProduct)
                                        .build();

                                    sb.append("detailImageUrl: ").append(detailImageUrl).append("\n");
                                }
                                System.out.println("######상세이미지 끝########");

                                // 색상 리스트
                                int options = driver.findElements(By.cssSelector("#root > div > div > div > div > div > select")).size();
                                List<WebElement> colorOptionTags = driver.findElements(By.cssSelector("#root > div > div > div > div > div > select:nth-child(1) > option"));
                                if(options==1){
                                    ProductColor productColor = ProductColor.builder()
                                            .product(newProduct)
                                            .color("단일색")
                                            .build();

                                    productColorRepository.save(productColor);

                                    sb.append("color: ").append("단일색");


                                    sb.append("\n");
                                    // 사이즈 리스트
                                    sb.append("size: ");
                                    List<WebElement> sizeOptionTags = driver.findElements(By.cssSelector("#root > div > div > div > div> table > tbody.product-detail__sc-swak4b-6  > tr > th"));
                                    for (int k = 0; k < sizeOptionTags.size() - 1; k++) {
                                        String size = sizeOptionTags.get(k + 1).getText().replace("(품절)", "").replaceAll("\\(마지막 \\d+개\\)$", "").trim();

                                        ProductSize productSize = ProductSize.builder()
                                                .productColor(productColor)
                                                .size(size)
                                                .stockQuantity(RandomNumber.generateRandomNumberBetween(1, 10))
                                                .build();

                                        productSizeRepository.save(productSize);

                                        sb.append(size).append(" ");
                                    }
                                } else {

                                    for (int j = 0; j < colorOptionTags.size()-1; j++) {
                                        String color = colorOptionTags.get(j + 1).getText();

                                        // 색상 생성
                                        ProductColor productColor = ProductColor.builder()
                                                .product(newProduct)
                                                .color(color)
                                                .build();

                                        productColorRepository.save(productColor);

                                        sb.append("color: ").append(color);


                                        sb.append("\n");


                                        // 사이즈 리스트
                                        sb.append("size: ");
                                        List<WebElement> sizeOptionTags = driver.findElements(By.cssSelector("#root > div > div > div > div> table > tbody.product-detail__sc-swak4b-6  > tr > th"));
                                        for (int k = 0; k < sizeOptionTags.size() - 1; k++) {
                                            String size = sizeOptionTags.get(k + 1).getText().replace("(품절)", "").replaceAll("\\(마지막 \\d+개\\)$", "").trim();

                                            ProductSize productSize = ProductSize.builder()
                                                    .productColor(productColor)
                                                    .size(size)
                                                    .stockQuantity(RandomNumber.generateRandomNumberBetween(1, 10))
                                                    .build();

                                            productSizeRepository.save(productSize);

                                            sb.append(size).append(" ");
                                        }

                                        sb.append("\n");
                                    }
                                }

                                // 태그
                                List<WebElement> tags = driver.findElements(By.cssSelector("#root > div > div > div > div > div.product-detail__sc-uwu3zm-0 > a"));
                                sb.append("tag: ");
                                for (WebElement webElement : tags) {
                                    String tag = webElement.getText().replace("#", "").trim();
                                    sb.append(tag).append(" ");

                                    // 태그가 이미 존재하는지 조회하고
                                    Optional<Tag> tagEntity = tagRepository.findByName(tag);

                                    Tag realTag;

                                    // 없으면
                                    if (tagEntity.isEmpty()) {
                                        // 태그 생성
                                        Tag newTag = Tag.builder()
                                            .name(tag)
                                            .build();
                                        realTag = tagRepository.save(newTag);
                                    } else {
                                        // 있으면
                                        realTag = tagEntity.get();
                                    }

                                    // 상품 태그 생성
                                    ProductTag productTag = ProductTag.builder()
                                            .product(newProduct)
                                            .tag(realTag)
                                            .build();
                                    productTagRepository.save(productTag);
                                }
                                // 상품 저장
                                Product save = productRepository.save(newProduct);
                                sb.append("\n");
                            }
                        }
                    }

                    System.out.println(sb);

                    // 브라우저 종료
//                    driver.quit();
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
