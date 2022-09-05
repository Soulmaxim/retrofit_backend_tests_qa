package ru.slmax.utils;

import com.github.javafaker.Faker;
import lombok.SneakyThrows;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import ru.slmax.db.dao.CategoriesMapper;
import ru.slmax.db.dao.ProductsMapper;
import ru.slmax.db.model.Categories;
import ru.slmax.db.model.CategoriesExample;
import ru.slmax.db.model.Products;
import ru.slmax.db.model.ProductsExample;
import ru.slmax.dto.Category;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DbUtils {

    public static String resource = "mybatisConfig.xml";

    static Faker faker = new Faker();

    private static SqlSession getSqlSession() throws IOException {
        SqlSessionFactory sqlSessionFactory;
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsStream(resource));
        return sqlSessionFactory.openSession(true);
    }

    @SneakyThrows
    public static CategoriesMapper getCategoriesMapper() {
        return getSqlSession().getMapper(CategoriesMapper.class);
    }

    @SneakyThrows
    public static ProductsMapper getProductMapper() {
        return getSqlSession().getMapper(ProductsMapper.class);
    }

    public static void createNewCategory(CategoriesMapper categoriesMapper) {
        Categories newCategory = new Categories();
        newCategory.setTitle(faker.animal().name());
        categoriesMapper.insert(newCategory);
    }

    public static Categories selectCategoryById(CategoriesMapper categoriesMapper, Integer id) {
        return categoriesMapper.selectByPrimaryKey(id);
    }

    public static List<Categories> selectAllCategory(CategoriesMapper categoriesMapper) {
        return categoriesMapper.selectByExample(new CategoriesExample());
    }

    public static Integer countCategories(CategoriesMapper categoriesMapper) {
        long categoriesCount = categoriesMapper.countByExample(new CategoriesExample());
        return Math.toIntExact(categoriesCount);
    }

    public static Integer countProducts(ProductsMapper productsMapper) {
        long products = productsMapper.countByExample(new ProductsExample());
        return Math.toIntExact(products);
    }

    public static void updateProductById(ProductsMapper productsMapper, Products product) {
        productsMapper.updateByPrimaryKey(product);
    }

    public static void deleteProductById(ProductsMapper productsMapper, Integer id) {
        productsMapper.deleteByPrimaryKey(Long.valueOf(id));
    }

    public static void deleteAllProducts(ProductsMapper productsMapper) {
        productsMapper.selectByExample(new ProductsExample());
    }

    public static Products selectProductById(ProductsMapper productsMapper, Long id) {
        return productsMapper.selectByPrimaryKey(id);
    }

    public static List<Products> selectAllProducts(ProductsMapper productsMapper) {
        return productsMapper.selectByExample(new ProductsExample());
    }
}
