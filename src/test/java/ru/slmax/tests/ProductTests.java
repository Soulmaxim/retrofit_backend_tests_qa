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
import ru.slmax.db.model.Categories;
import ru.slmax.db.model.Products;
import ru.slmax.dto.Product;
import ru.slmax.enums.CategoryType;
import ru.slmax.service.CategoryService;
import ru.slmax.service.ProductService;
import ru.slmax.utils.DbUtils;
import ru.slmax.utils.PrettyLogger;
import ru.slmax.utils.RetrofitUtils;
import ru.slmax.db.dao.ProductsMapper;

import java.io.IOException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class ProductTests {
    Integer id;
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
    }

    @Test
    void createProductWithIdTest() throws IOException {
        product.setId(13239);
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        Response<Product> response = productService.createProduct(product).execute();
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        assertThat(response.code(), equalTo(400));
        assertThat(countProductsAfter, equalTo(countProductsBefore));
    }

    @Test
    void createProductWithNullFieldTest() throws IOException {
        product.setPrice(null);
        product.setTitle(null);
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        Response<Product> response = productService.createProduct(product).execute();
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        assertThat(response.code(), equalTo(400));
        assertThat(countProductsAfter, equalTo(countProductsBefore));
    }

    @Test
    void createProductWithNonexistentCategoryTest() throws IOException {
        List<Categories> allCategories = DbUtils.selectAllCategory(categoriesMapper);
        String newTitle = allCategories.get(0).getTitle() + "o";
        boolean check = true;
        while(check) {
            check = false;
            for (Categories i : allCategories) { // проверка, что категории не существует
                if (newTitle.equals(i.getTitle())) {
                    check = true;
                    newTitle += "o";
                    break;
                }
            }
        }
        product.setCategoryTitle(newTitle);

        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        Response<Product> response = productService.createProduct(product).execute();
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        assertThat(response.code(), equalTo(400));
        assertThat(countProductsAfter, equalTo(countProductsBefore));
    }

    @Test
    void getProductsByIdTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        assert response.body() != null;
        id = response.body().getId();
        response = productService
                .getProduct(id)
                .execute();
        if (response.body() != null) prettyLogger.log(response.body().toString());
        assertThat(response.code(), equalTo(200));
        assertThat(response.body().getId(), equalTo(id));
        assertThat(response.body().getTitle(), equalTo(product.getTitle()));
        assertThat(response.body().getPrice(), equalTo(product.getPrice()));
        assertThat(response.body().getCategoryTitle(), equalTo(product.getCategoryTitle()));
    }

    @Test
    void getAfterDeleteProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        assert response.body() != null;
        id = response.body().getId();
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        DbUtils.deleteProductById(productsMapper, id);
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        response = productService
                .getProduct(id)
                .execute();
        assertThat(response.code(), equalTo(404));
        assertThat(countProductsAfter, equalTo(countProductsBefore - 1));
    }

    @Test
    void getProductByNonexistentIdTest() throws IOException {
        List<Products> allProducts = DbUtils.selectAllProducts(productsMapper);
        boolean check = true;
        int nonId;
        while(check) {
            check = false;
            nonId = (int)((Math.random() * 90000) + 10000);
            for (Products i : allProducts) { // проверка, что такого id не существует
                if (i.getId().equals(Long.valueOf(nonId))) {
                    check = true;
                    break;
                }
            }
        }
        Response<Product> response = productService
                .getProduct(-1)
                .execute();
        assertThat(response.code(), equalTo(404));
    }

    @Test
    void getAllProductsTest() {
        List<Products> allProducts = DbUtils.selectAllProducts(productsMapper);
        for (Products i : allProducts) {
            assertThat(i.getId().toString(), Matchers.matchesPattern("^[0-9]{1,5}"));
            assertThat(i.getTitle(), Matchers.is(notNullValue()));
            assertThat(i.getPrice(), Matchers.is(notNullValue()));
            assertThat(i.getCategory_id(), Matchers.is(notNullValue()));
        }
    }

    @Test
    void updateProductTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        assert response.body() != null;
        id = response.body().getId();
        Products productDB = new Products();
        Integer price = 1500;
        String title = "New title";
        productDB.setId(Long.valueOf(id));
        productDB.setPrice(price);
        productDB.setTitle(title);
        productDB.setCategory_id(Long.valueOf(CategoryType.FURNITURE.getId()));
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        DbUtils.updateProductById(productsMapper, productDB);
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        Response<Product> responseUpd = productService
                .getProduct(id)
                .execute();
        assertThat(responseUpd.code(), equalTo(200));
        assertThat(countProductsAfter, equalTo(countProductsBefore));
        assertThat(responseUpd.body().getPrice(), equalTo(price));
        assertThat(responseUpd.body().getTitle(), equalTo(title));
        assertThat(responseUpd.body().getCategoryTitle(), equalTo(CategoryType.FURNITURE.getTitle()));
    }

    @Test
    void updateProductWithoutIdTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        assert response.body() != null;
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
        Products productDB = DbUtils.selectProductById(productsMapper, Long.valueOf(id));
        assertThat(responseUpd.code(), equalTo(400));
        assertThat(productDB.getPrice(), equalTo(response.body().getPrice()));
        assertThat(productDB.getTitle(), equalTo(response.body().getTitle()));
    }

    @Test
    void updateWithNullFieldTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        assert response.body() != null;
        id = response.body().getId();
        Integer price = null;
        String title = null;
        product.setId(id);
        product.setPrice(price);
        product.setTitle(title);
        Response<Product> responseUpd = productService
                .updateProduct(product)
                .execute();
        if (responseUpd.body() != null) prettyLogger.log(responseUpd.body().toString());
        Products productDB = DbUtils.selectProductById(productsMapper, Long.valueOf(id));
        assertThat(responseUpd.code(), equalTo(400));
        assertThat(productDB.getPrice(), equalTo(response.body().getPrice()));
        assertThat(productDB.getTitle(), equalTo(response.body().getTitle()));
    }

    @Test
    void updateProductAfterDeleteTest() throws IOException {
        Response<Product> response = productService
                .createProduct(product)
                .execute();
        assert response.body() != null;
        id = response.body().getId();
        DbUtils.deleteProductById(productsMapper, id);
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
        Products productDB = DbUtils.selectProductById(productsMapper, Long.valueOf(id));
        assertThat(responseUpd.code(), equalTo(404));
        assertThat(productDB, equalTo(null));
    }

    @Test
    void deleteProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        assert response.body() != null;
        id = response.body().getId();
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        DbUtils.deleteProductById(productsMapper, id);
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        assertThat(countProductsAfter, equalTo(countProductsBefore - 1));
    }

    @Test
    void deleteAfterDeleteProductTest() throws IOException {
        Response<Product> response = productService.createProduct(product).execute();
        assert response.body() != null;
        id = response.body().getId();
        Integer countProductsBefore = DbUtils.countProducts(productsMapper);
        DbUtils.deleteProductById(productsMapper, id);
        Integer countProductsAfter = DbUtils.countProducts(productsMapper);
        Response<ResponseBody> responseDel = productService
                .deleteProduct(id)
                .execute();
        Integer countProductsLast = DbUtils.countProducts(productsMapper);
        assertThat(responseDel.code(), equalTo(404));
        assertThat(countProductsBefore, equalTo(countProductsAfter+1));
        assertThat(countProductsAfter, equalTo(countProductsLast));
    }

    @Test
    void getCategoryByIdTest() {
        Integer idCategory = CategoryType.FOOD.getId();
        Categories categories = DbUtils.selectCategoryById(categoriesMapper, idCategory);
        assertThat(categories.getTitle(), equalTo(CategoryType.FOOD.getTitle()));
        assertThat(categories.getId(), equalTo(idCategory));
    }
}
