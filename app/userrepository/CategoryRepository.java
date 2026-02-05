package com.zstore.app.userrepository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.zstore.app.entities.Categories;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {

    Optional<Categories> findByCategoryName(String categoryName);
}