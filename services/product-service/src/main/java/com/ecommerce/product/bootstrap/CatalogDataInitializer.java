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

        // === Categories ===
        Category electronics = saveCategory("电子产品", null, 1);
        Category home = saveCategory("家居生活", null, 2);
        Category clothing = saveCategory("服装服饰", null, 3);
        Category food = saveCategory("食品饮料", null, 4);
        Category beauty = saveCategory("美妆护肤", null, 5);
        Category sports = saveCategory("运动户外", null, 6);

        Category phones = saveCategory("手机通讯", electronics.getId(), 1);
        Category laptops = saveCategory("电脑办公", electronics.getId(), 2);
        Category audio = saveCategory("影音娱乐", electronics.getId(), 3);
        Category kitchen = saveCategory("厨房用品", home.getId(), 1);
        Category bedding = saveCategory("床上用品", home.getId(), 2);
        Category menswear = saveCategory("男装", clothing.getId(), 1);
        Category womenswear = saveCategory("女装", clothing.getId(), 2);
        Category snacks = saveCategory("休闲零食", food.getId(), 1);
        Category beverages = saveCategory("饮料冲调", food.getId(), 2);
        Category skincare = saveCategory("面部护理", beauty.getId(), 1);
        Category makeup = saveCategory("彩妆", beauty.getId(), 2);
        Category outdoor = saveCategory("户外装备", sports.getId(), 1);
        Category fitness = saveCategory("健身器材", sports.getId(), 2);

        // === Brands ===
        Brand huawei = saveBrand("华为");
        Brand xiaomi = saveBrand("小米");
        Brand apple = saveBrand("Apple");
        Brand lenovo = saveBrand("联想");
        Brand midea = saveBrand("美的");
        Brand supOR = saveBrand("苏泊尔");
        Brand nike = saveBrand("耐克");
        Brand adidas = saveBrand("阿迪达斯");
        Brand loreal = saveBrand("欧莱雅");
        Brand estee = saveBrand("雅诗兰黛");
        Brand threeSquirrels = saveBrand("三只松鼠");
        Brand yili = saveBrand("伊利");
        Brand anta = saveBrand("安踏");

        // === Products ===
        // 手机通讯 (5)
        saveProduct("SKU-PHONE-001", "华为 Mate 60 Pro", "麒麟 9000S 芯片 | 6.82英寸 OLED | 5000万像素主摄 | 88W快充 | 卫星通话", "6999.00", huawei, phones, 120, true,
                "https://images.unsplash.com/photo-1616348436168-de43ad0db5ca?w=400&h=400&fit=crop");
        saveProduct("SKU-PHONE-002", "小米 14 Pro", "骁龙8 Gen3 | 6.73英寸 2K屏 | 徕卡光学镜头 | 120W秒充 | IP68防水", "4999.00", xiaomi, phones, 200, true,
                "https://images.unsplash.com/photo-1580910051074-3eb694886505?w=400&h=400&fit=crop");
        saveProduct("SKU-PHONE-003", "iPhone 15 Pro Max", "A17 Pro芯片 | 6.7英寸超视网膜XDR | 钛金属设计 | 4800万主摄 | USB-C", "9999.00", apple, phones, 80, true,
                "https://images.unsplash.com/photo-1573310605866-149345343c4c?w=400&h=400&fit=crop");
        saveProduct("SKU-PHONE-004", "华为 nova 12", "麒麟 8000 | 6.7英寸 OLED | 6000万前置 | 100W快充", "2999.00", huawei, phones, 150, false,
                "https://images.unsplash.com/photo-1633051644822-7d00f7ae86f8?w=400&h=400&fit=crop");
        saveProduct("SKU-PHONE-005", "小米 Redmi Note 13 Pro", "骁龙7s Gen2 | 6.67英寸 | 2亿像素主摄 | 67W快充 | 5100mAh", "1699.00", xiaomi, phones, 300, true,
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=400&h=400&fit=crop");

        // 电脑办公 (5)
        saveProduct("SKU-LAPTOP-001", "联想 ThinkPad X1 Carbon", "14英寸 2.8K OLED | i7-1365U | 32GB | 1TB SSD | 商务旗舰", "10999.00", lenovo, laptops, 40, true,
                "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=400&fit=crop");
        saveProduct("SKU-LAPTOP-002", "MacBook Pro 14", "M3 Pro芯片 | 14.2英寸 Liquid Retina XDR | 18GB | 512GB | 深空黑", "14999.00", apple, laptops, 30, true,
                "https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=400&h=400&fit=crop");
        saveProduct("SKU-LAPTOP-003", "华为 MateBook X Pro", "14.2英寸 3.1K触控屏 | i7-1360P | 16GB | 1TB | 微绒美学", "8999.00", huawei, laptops, 50, true,
                "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?w=400&h=400&fit=crop");
        saveProduct("SKU-LAPTOP-004", "小米笔记本 Pro 16", "16英寸 3.1K | i5-13500H | 16GB | 512GB | 轻薄机身", "5999.00", xiaomi, laptops, 60, false,
                "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=400&h=400&fit=crop");
        saveProduct("SKU-LAPTOP-005", "联想拯救者 Y9000P", "16英寸 2.5K 240Hz | i9-13900HX | RTX 4060 | 16GB | 1TB", "9999.00", lenovo, laptops, 25, true,
                "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=400&h=400&fit=crop");

        // 影音娱乐 (3)
        saveProduct("SKU-AUDIO-001", "Apple AirPods Pro 2", "H2芯片 | 主动降噪 | 自适应音频 | USB-C | 个性化空间音频", "1899.00", apple, audio, 200, true,
                "https://images.unsplash.com/photo-1606841837239-c5a1a4a07af7?w=400&h=400&fit=crop");
        saveProduct("SKU-AUDIO-002", "华为 FreeBuds Pro 3", "星闪连接 | 智慧动态降噪 | 高清空间音频 | IP54防水", "1399.00", huawei, audio, 150, true,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=400&fit=crop");
        saveProduct("SKU-AUDIO-003", "小米 Sound Pro 音箱", "哈曼卡顿调音 | 360°环绕声 | 全屋播放 | 小爱同学 | 智能家居中心", "599.00", xiaomi, audio, 100, false,
                "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=400&h=400&fit=crop");

        // 厨房用品 (3)
        saveProduct("SKU-KITCHEN-001", "美的电饭煲 MB-FB40", "4L容量 | IH电磁加热 | 24小时预约 | 智能菜单 | 不粘内胆", "399.00", midea, kitchen, 180, true,
                "https://images.unsplash.com/photo-1556909114-f6e7d7a0737a?w=400&h=400&fit=crop");
        saveProduct("SKU-KITCHEN-002", "苏泊尔破壁机 SP902", "1.75L | 35000转/分 | 12大功能 | 静音降噪 | 自动清洗", "699.00", supOR, kitchen, 90, true,
                "https://images.unsplash.com/photo-1635264034409-8443f2e1a781?w=400&h=400&fit=crop");
        saveProduct("SKU-KITCHEN-003", "美的空气炸锅 KZ-35", "3.5L | 360°热风循环 | 无油炸 | 8大预设菜单 | 不粘涂层", "299.00", midea, kitchen, 150, false,
                "https://images.unsplash.com/photo-1625937281961-1ef23fb0cadd?w=400&h=400&fit=crop");

        // 男装 (3)
        saveProduct("SKU-MEN-001", "耐克 Dri-FIT 跑步T恤", "速干面料 | 透气排汗 | 反光元素 | 多色可选 | S-3XL", "199.00", nike, menswear, 500, true,
                "https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=400&fit=crop");
        saveProduct("SKU-MEN-002", "阿迪达斯运动长裤", "常规版型 | 棉涤混纺 | 侧边条纹 | 束脚设计 | 经典三条纹", "349.00", adidas, menswear, 300, true,
                "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=400&h=400&fit=crop");
        saveProduct("SKU-MEN-003", "安踏篮球鞋 KT8", "氮科技中底 | 碳板支撑 | 耐磨橡胶外底 | 透气鞋面 | 汤普森签名", "699.00", anta, menswear, 120, false,
                "https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?w=400&h=400&fit=crop");

        // 休闲零食 (3)
        saveProduct("SKU-SNACK-001", "三只松鼠每日坚果礼盒", "750g/30袋 | 6种坚果果干 | 科学配比 | 独立包装 | 年货送礼", "139.00", threeSquirrels, snacks, 600, true,
                "https://images.unsplash.com/photo-1599599810769-bcde5a160d32?w=400&h=400&fit=crop");
        saveProduct("SKU-SNACK-002", "伊利安慕希原味酸奶", "205g×12盒 | 希腊风味 | 进口菌种 | 早餐搭档 | 浓郁醇厚", "59.90", yili, snacks, 1000, true,
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?w=400&h=400&fit=crop");
        saveProduct("SKU-SNACK-003", "三只松鼠手撕面包", "1kg整箱 | 奶香浓郁 | 层层起酥 | 独立包装 | 早餐零食", "39.90", threeSquirrels, snacks, 800, false,
                "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=400&h=400&fit=crop");

        // 面部护理 (3)
        saveProduct("SKU-SKIN-001", "欧莱雅男士水能保湿套装", "洁面+水+乳液三件套 | 清爽补水 | 控油保湿 | 温和不刺激", "159.00", loreal, skincare, 400, true,
                "https://images.unsplash.com/photo-1596462502278-27bfdc403348?w=400&h=400&fit=crop");
        saveProduct("SKU-SKIN-002", "雅诗兰黛小棕瓶精华液", "第七代 | 50ml | 修护抗老 | 弹嫩透亮 | 夜间修护", "990.00", estee, skincare, 60, true,
                "https://images.unsplash.com/photo-1611930022073-b7a4ba5fcccd?w=400&h=400&fit=crop");
        saveProduct("SKU-SKIN-003", "欧莱雅复颜玻尿酸面膜", "15片装 | 三重玻尿酸 | 深层补水 | 紧致淡纹 | 贴合面部", "89.00", loreal, skincare, 500, true,
                "https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=400&h=400&fit=crop");

        // 户外装备 (2)
        saveProduct("SKU-OUTDOOR-001", "安踏户外冲锋衣", "防风防水 | 透气面料 | 可拆卸内胆 | 多口袋设计 | 男女同款", "459.00", anta, outdoor, 200, true,
                "https://images.unsplash.com/photo-1605723145513-9f1c5235c1d2?w=400&h=400&fit=crop");
        saveProduct("SKU-OUTDOOR-002", "耐克户外双肩包", "30L容量 | 防水面料 | 人体工学背负 | 电脑隔层 | 多仓收纳", "259.00", nike, outdoor, 250, false,
                "https://images.unsplash.com/photo-1546938570-6e1b0a4b2422?w=400&h=400&fit=crop");

        reindexIfSearchEmpty();
        log.info("Catalog seed complete — {} products across {} categories and {} brands",
                productRepository.count(), categoryRepository.count(), brandRepository.count());
    }

    private void reindexIfSearchEmpty() {
        if (productIndexService.countIndexedProducts() > 0) {
            return;
        }
        log.info("Elasticsearch index empty — reindexing all products...");
        List<Product> products = productRepository.findAll();
        products.forEach(p -> {
            productRepository.findDetailedById(p.getId()).ifPresent(detail -> {
                productIndexService.indexProduct(detail);
                productEventPublisher.publishUpdated(detail.getId());
            });
        });
        log.info("Reindexed {} products into Elasticsearch", products.size());
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
            String sku, String name, String description, String price,
            Brand brand, Category category, int stock, boolean featured, String imageUrl) {
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
