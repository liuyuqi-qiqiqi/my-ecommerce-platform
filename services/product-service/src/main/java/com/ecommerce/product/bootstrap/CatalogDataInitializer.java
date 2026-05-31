package com.ecommerce.product.bootstrap;

import com.ecommerce.product.domain.Brand;
import com.ecommerce.product.domain.Category;
import com.ecommerce.product.domain.Product;
import com.ecommerce.product.domain.ProductImage;
import com.ecommerce.product.domain.ProductStatus;
import com.ecommerce.product.event.ProductEventPublisher;
import com.ecommerce.product.repository.BrandRepository;
import com.ecommerce.product.repository.CategoryRepository;
import com.ecommerce.product.repository.ProductRepository;
import com.ecommerce.product.service.ProductIndexService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogDataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogDataInitializer.class);

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductIndexService productIndexService;
    private final ProductEventPublisher productEventPublisher;

    public CatalogDataInitializer(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            ProductIndexService productIndexService,
            ProductEventPublisher productEventPublisher) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productIndexService = productIndexService;
        this.productEventPublisher = productEventPublisher;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (productRepository.countByStatus(ProductStatus.ACTIVE) > 0) {
            reindexIfSearchEmpty();
            return;
        }

        log.info("Seeding catalog data...");
        Category electronics = saveCategory("电子产品", null, 1);
        Category home = saveCategory("家居生活", null, 2);
        Category phones = saveCategory("手机", electronics.getId(), 1);
        Category laptops = saveCategory("笔记本电脑", electronics.getId(), 2);

        Brand techBrand = saveBrand("TechBrand");
        Brand homePlus = saveBrand("HomePlus");
        Brand mobileOne = saveBrand("MobileOne");

        saveProduct(
                "SKU-PHONE-001",
                "智能手机 Pro",
                "6.7 英寸 OLED 显示屏，256GB 存储",
                "4999.00",
                mobileOne,
                phones,
                50,
                true,
                "https://placehold.co/400x400?text=Phone+Pro");
        saveProduct(
                "SKU-LAPTOP-001",
                "轻薄笔记本 Air",
                "14 英寸，16GB 内存，512GB SSD",
                "6999.00",
                techBrand,
                laptops,
                30,
                true,
                "https://placehold.co/400x400?text=Laptop+Air");
        saveProduct(
                "SKU-HOME-001",
                "智能台灯",
                "可调色温，App 控制",
                "299.00",
                homePlus,
                home,
                100,
                true,
                "https://placehold.co/400x400?text=Smart+Lamp");
        saveProduct(
                "SKU-PHONE-002",
                "入门智能手机",
                "6.1 英寸 LCD，128GB 存储",
                "1999.00",
                mobileOne,
                phones,
                80,
                false,
                "https://placehold.co/400x400?text=Phone+Lite");
        saveProduct(
                "SKU-LAPTOP-002",
                "游戏笔记本",
                "15.6 英寸，RTX 显卡，32GB 内存",
                "9999.00",
                techBrand,
                laptops,
                15,
                false,
                "https://placehold.co/400x400?text=Gaming+Laptop");

        reindexIfSearchEmpty();
        log.info("Catalog seed complete");
    }

    private void reindexIfSearchEmpty() {
        if (productIndexService.countIndexedProducts() > 0) {
            return;
        }
        log.info("Elasticsearch index empty — reindexing all products");
        List<Product> products = productRepository.findAll();
        products.forEach(product -> {
            productRepository.findDetailedById(product.getId()).ifPresent(p -> {
                productIndexService.indexProduct(p);
                productEventPublisher.publishUpdated(p.getId());
            });
        });
    }

    private Category saveCategory(String name, Long parentId, int sortOrder) {
        Category category = new Category();
        category.setName(name);
        category.setParentId(parentId);
        category.setSortOrder(sortOrder);
        return categoryRepository.save(category);
    }

    private Brand saveBrand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        return brandRepository.save(brand);
    }

    private void saveProduct(
            String sku,
            String name,
            String description,
            String price,
            Brand brand,
            Category category,
            int stock,
            boolean featured,
            String imageUrl) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(new BigDecimal(price));
        product.setBrand(brand);
        product.setCategory(category);
        product.setStockQuantity(stock);
        product.setFeatured(featured);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(Instant.now());
        product.setUpdatedAt(Instant.now());

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setUrl(imageUrl);
        image.setPrimaryImage(true);
        image.setSortOrder(0);
        product.getImages().add(image);

        Product saved = productRepository.save(product);
        productEventPublisher.publishCreated(saved.getId());
    }
}
