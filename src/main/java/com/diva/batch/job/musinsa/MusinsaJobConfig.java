package com.diva.batch.job.musinsa;

import static com.diva.batch.entity.fitme.Gender.fromValue;

import com.diva.batch.entity.fitme.Brand;
import com.diva.batch.entity.fitme.Category;
import com.diva.batch.entity.fitme.DetailImage;
import com.diva.batch.entity.fitme.MainImage;
import com.diva.batch.entity.fitme.Product;
import com.diva.batch.entity.fitme.ProductOption;
import com.diva.batch.entity.fitme.ProductTag;
import com.diva.batch.entity.fitme.Tag;
import com.diva.batch.repository.BrandRepository;
import com.diva.batch.repository.CategoryRepository;
import com.diva.batch.repository.ProductRepository;
import com.diva.batch.repository.TagRepository;
import com.diva.batch.utils.RandomNumber;
import jakarta.persistence.EntityManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ProductRepository productRepository;

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
                    for (int i = 0; i < 1; i++) { // ToDo: 나중에 17로 변경할거야
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
                        for (int page = 1; page <= 1; page++) { // ToDo: 나중에 10으로 변경할거야
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

                            // 한 페이지에서 상품을 순회한다.
                            for (int item = 0; item < 3; item++) { // ToDo: 나중에 90으로 변경
                                // 상품 상세 페이지 접근
                                driver.get(linkUrl.get(item));

                                // 브랜드
                                String brandName = driver.findElement(By.cssSelector("#root > div > div > div > div > ul > li:nth-child(1) > div > a")).getText().trim();
                                // 브랜드 없으면 생성
                                // 브랜드 조회
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

                                // 이름
                                String productName = driver.findElement(By.cssSelector("#root > div > div > div> div> h3")).getText().trim();
                                sb.append("productName: ").append(productName).append("\n");

                                // 성별
                                String gender = driver.findElement(By.cssSelector("#root > div> div > div > div> ul > li:nth-child(2) > div > span:last-child")).getText();
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

                                // 나이대
                                String ageRange = driver.findElement(By.cssSelector("#root > div > div > div > button > strong")).getText().replace(",", "").replace("세 이상", "").trim();
                                sb.append("ageRange: ").append(ageRange).append("\n");

                                // 가격
                                int price = Integer.parseInt(driver.findElement(By.cssSelector("#root > div > div > div > div > div > ul > li:nth-child(1) > div:nth-child(2) > span")).getText().replace(",", "").replace("원", ""));
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

                                // 메인 이미지
                                List<WebElement> imageTags = driver.findElements(By.cssSelector("#root > div > div > div > div > ul > li > img"));
                                for (WebElement imageTag : imageTags) {
                                    String imageUrl = imageTag.getAttribute("src");

                                    MainImage.builder()
                                        .url(imageUrl)
                                        .product(newProduct)
                                        .build();

                                    sb.append("imageUrl: ").append(imageUrl).append("\n");

                                    // ToDo: 이미지 다운로드
                                    // product의 id로 폴더를 만들고 그 안에 이미지를 저장한다.
                                    // 이름은 prd_img/로
                                }

                                // 상세 이미지 리스트
                                List<WebElement> detailImageTags = driver.findElements(By.cssSelector("#root > div:nth-child(3) > section > div > div > div > div > div> img"));
                                for (WebElement detailImageTag : detailImageTags) {
                                    String detailImageUrl = detailImageTag.getAttribute("src");

                                    DetailImage.builder()
                                        .url(detailImageUrl)
                                        .product(newProduct)
                                        .build();

                                    sb.append("detailImageUrl: ").append(detailImageUrl).append("\n");
                                }

                                // 색상 옵션 리스트
                                List<WebElement> colorOptionTags = driver.findElements(By.cssSelector("#root > div > div > div > div > div > select:nth-child(1) > option"));
                                List<String> colorOptions = new ArrayList<>();
                                for (int j = 0; j < colorOptionTags.size()-1; j++) {
                                    colorOptions.add(colorOptionTags.get(j+1).getText());
                                }

                                // 사이즈 옵션 리스트
                                List<WebElement> sizeOptionTags = driver.findElements(By.cssSelector("#root > div > div > div > div > table > tbody > tr > th"));
                                List<String> sizeOptions = new ArrayList<>();
                                for (int j = 0; j < sizeOptionTags.size() - 1; j++) {
                                    sizeOptions.add(sizeOptionTags.get(j + 1).getText().replace("(품절)", "").replaceAll("\\(마지막 \\d+개\\)$", "").trim());
                                }

                                // 옵션 생성
                                for (String colorOption : colorOptions) {
                                    for (String sizeOption : sizeOptions) {
                                        // 옵션 생성
                                        ProductOption.builder()
                                                .product(newProduct)
                                                .color(colorOption)
                                                .size(sizeOption)
                                                .stockQuantity(RandomNumber.generateRandomNumberBetween(1, 10))
                                                .build();

                                        sb.append("color: ").append(colorOption).append(" size: ")
                                            .append(sizeOption).append("\n");
                                    }
                                }

                                // 태그
                                List<WebElement> tags = driver.findElements(By.cssSelector("#root > div > div > div > div > div > a"));
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
                                    ProductTag.builder()
                                        .product(newProduct)
                                        .tag(realTag)
                                        .build();
                                }
                                System.out.println(brandName);
                                // 상품 저장
                                productRepository.save(newProduct);
                                sb.append("\n");
                            }
                        }
                    }

                    System.out.println(sb);

                    // 브라우저 종료
                    driver.quit();
                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
