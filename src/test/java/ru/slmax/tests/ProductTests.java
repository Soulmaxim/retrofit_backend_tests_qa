package ru.slmax.tests;

import com.github.javafaker.Faker;
import okhttp3.ResponseBody;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import ru.slmax.db.dao.CategoriesMapper;
import ru.slmax.dto.Category;
import ru.slmax.dto.Product;
import ru.slmax.enums.CategoryType;
import ru.slmax.service.CategoryService;
import ru.slmax.service.ProductService;
import ru.slmax.utils.DbUtils;
import ru.slmax.utils.PrettyLogger;
import ru.slmax.utils.RetrofitUtils;
import ru.slmax.db.dao.ProductsMapper;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ProductTests {
    int id;
    static ProductsMapper productsMapper;
    static CategoriesMapper categoriesMapper;
    static Retrofit client;
    static ProductService productService;
    static CategoryService categoryService;
    Product product;
    Faker faker = new Faker();
    PrettyLogger prettyLogger = new PrettyLogger();

    @BeforeAll
    static void beforeAll() {
        client = RetrofitUtils.getRetrofit();
        productService = client.create(ProductService.class);
        categoryService = client.create(CategoryService.class);
        productsMapper = DbUtils.getProductMapper();
        categoriesMapper = DbUtils.getCategoriesMapper();
    }

    @BeforeEach
    void setUp() {
        product = new Product()
                .withTitle(faker.food().dish())
                .withCategoryTitle(CategoryType.FOOD.getTitle())
                .withPrice((int) ((Math.random() + 1) * 100));
    }

    @Test
    void createProductTest() throws IOException {
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        Response<Product> response = productService.createProduct(product).execute();
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        assertThat(countProductsAfter, equalTo(countProductsBefore+1));
        assertThat(response.code(), equalTo(201));
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
        prettyLogger.log(response.body().toString());
        id = response.body().getId();
    }

    @Test
    void createProductWithIdTest() throws IOException {
        product.setId(13239);
        Response<Product> response = productService.createProduct(product).execute();
        assertThat(response.code(), equalTo(400));
    }

    @Test
    void createProductWithNullFieldTest() throws IOException {
        product.setPrice(null);
        product.setTitle(null);
        Response<Product> response = productService.createProduct(product).execute();
        assertThat(response.code(), equalTo(400));
    }

    @Test
    void createProductWithOtherCategoryTest() throws IOException {
        product.setCategoryTitle("Other");
        Response<Product> response = productService.createProduct(product).execute();
        assertThat(response.code(), equalTo(201));
        if (response.code() == 201) prettyLogger.log(response.body().toString());
    }

    @Test
    void getProductsByIdTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        id = response.body().getId();
        response = productService
                .getProduct(id)
                .execute();
        prettyLogger.log(response.body().toString());
        assertThat(response.code(), equalTo(200));
        assertThat(response.body().getId(), equalTo(id));
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    void getAfterDeleteProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        id = response.body().getId();
        Response<ResponseBody> responseDel = productService
                .deleteProduct(id)
                .execute();
        response = productService
                .getProduct(id)
                .execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    void getADeleteProductTest() throws IOException {
        Response<Product> response = productService
                .getProduct(-1)
                .execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    void getAllProductsTest() throws IOException {
        Response<ArrayList<Product>> response = productService
                .getProducts()
                .execute();
        prettyLogger.log(response.body().toString());
        for (int i = 0; i < response.body().size(); i++) {
            assertThat(response.code(), equalTo(200));
            assertThat(response.body().get(i).getId().toString(), Matchers.matchesPattern("^[0-9]{1,5}"));
            assertThat(response.body().get(i).getTitle(), Matchers.is(notNullValue()));
            assertThat(response.body().get(i).getPrice(), Matchers.is(notNullValue()));
            assertThat(response.body().get(i).getCategoryTitle(), Matchers.is(notNullValue()));
        }
    }

    @Test
    void updateProductTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        id = response.body().getId();
        Integer price = 1500;
        String title = "New title";
        String category = "Furniture";
        product.setId(id);
        product.setPrice(price);
        product.setTitle(title);
        product.setCategoryTitle(category);
        Response<Product> responseUpd = productService
                .updateProduct(product)
                .execute();
        prettyLogger.log(responseUpd.body().toString());
        assertThat(responseUpd.code(), equalTo(200));
        assertThat(responseUpd.body().getPrice(), equalTo(price));
        assertThat(responseUpd.body().getTitle(), equalTo(title));
        assertThat(responseUpd.body().getCategoryTitle(), equalTo(category));
    }

    @Test
    void updateProductWithoutIdTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        id = response.body().getId();
        Integer price = 1500;
        String title = "New title";
        String category = "Furniture";
        product.setPrice(price);
        product.setTitle(title);
        product.setCategoryTitle(category);
        Response<Product> responseUpd = productService
                .updateProduct(product)
                .execute();
        assertThat(responseUpd.code(), equalTo(400));
    }

    @Test
    void updateWithNullFieldTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        id = response.body().getId();
        Integer price = null;
        String title = null;
        product.setId(id);
        product.setPrice(price);
        product.setTitle(title);
        Response<Product> responseUpd = productService
                .updateProduct(product)
                .execute();
        prettyLogger.log(responseUpd.body().toString());
        assertThat(responseUpd.code(), equalTo(400));
    }

    @Test
    void updateProductAfterDeleteTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        id = response.body().getId();
        Response<ResponseBody> responseDel = productService
                .deleteProduct(id)
                .execute();
        Integer price = 1500;
        String title = "New title";
        String category = "Furniture";
        product.setId(id);
        product.setPrice(price);
        product.setTitle(title);
        product.setCategoryTitle(category);
        System.out.println(product);
        Response<Product> responseUpd = productService
                .updateProduct(product)
                .execute();
        assertThat(responseUpd.code(), equalTo(404));
    }

    @Test
    void deleteProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        id = response.body().getId();
        Response<ResponseBody> responseDel = productService
                .deleteProduct(id)
                .execute();
        assertThat(responseDel.code(), equalTo(200));
    }

    @Test
    void deleteAfterDeleteProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        id = response.body().getId();
        Response<ResponseBody> responseDel = productService
                .deleteProduct(id)
                .execute();
        responseDel = productService
                .deleteProduct(id)
                .execute();
        assertThat(responseDel.code(), equalTo(404));
    }

    @Test
    void getCategoryByIdTest() throws IOException {
        DbUtils.createNewCategory(categoriesMapper);
        // удаление через бд
        // создать, обновить через бд, получить
        // создавать лучше через api

        // встроить проверки через бд
        /*
        countCategories
        countProducts
        createNewCategory

        deleteByPrimaryKey
        deleteByExample
        selectByExample
         */
        // сделать апдейты и удаления через бд
        Integer idCategory = CategoryType.FOOD.getId();
        Response<Category> response = categoryService
                .getCategory(idCategory)
                .execute();
        prettyLogger.log(response.body().toString());
        assertThat(response.body().getTitle(), equalTo(CategoryType.FOOD.getTitle()));
        assertThat(response.body().getId(), equalTo(idCategory));
    }
}
