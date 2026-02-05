package com.zstore.app.userservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.zstore.app.entities.Categories;
import com.zstore.app.entities.Product;
import com.zstore.app.entities.ProductImage;
import com.zstore.app.userrepository.CategoryRepository;
import com.zstore.app.userrepository.ProductImageRepository;
import com.zstore.app.userrepository.ProductRepository;
import com.zstore.app.userservicecontract.ProductServiceContract;

@Service
public class ProductService implements ProductServiceContract {
	private ProductRepository productRepository;
	private ProductImageRepository productImageRepository;
	private CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, CategoryRepository categoryRepository) {
		super();
		this.productRepository = productRepository;
		this.productImageRepository = productImageRepository;
		this.categoryRepository = categoryRepository;
	}

	@Override
	public List<Product> getProductsByCategory(String categoryName) {
		 if (categoryName != null && !categoryName.isEmpty()) {

	            Optional<Categories> categoryOpt =
	                    categoryRepository.findByCategoryName(categoryName);

	            if (categoryOpt.isPresent()) {
	            	Categories category = categoryOpt.get();
	                return productRepository
	                        .findByCategory_CategoryId(category.getCategoryId());
	            } else {
	                throw new RuntimeException("Category not found");
	            }

	        } else {
	            return productRepository.findAll();
	        }
	}

	@Override
	public List<String> getProductImages(Integer productId) {
		List<ProductImage> productImages = productImageRepository.findByProduct_ProductId(productId);

        List<String> imageUrls = new ArrayList<>();

        for (ProductImage image : productImages) {
            imageUrls.add(image.getImageUrl());
        }

        return imageUrls;
	}

}
