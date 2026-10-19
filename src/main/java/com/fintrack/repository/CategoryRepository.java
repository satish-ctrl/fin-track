
package com.fintrack.repository;

import com.fintrack.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findAllByUser_EmailOrderByNameAsc(String email);

    Optional<Category> findByIdAndUser_Email(Long categoryId, String email);

    boolean existsByUser_EmailAndNameIgnoreCase(String email, String name);
}
