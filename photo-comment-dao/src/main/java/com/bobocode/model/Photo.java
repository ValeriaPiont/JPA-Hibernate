package com.bobocode.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter
@Setter
@Table(name = "photo")
@Entity
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String url;
    private String description;

    @Setter(AccessLevel.PRIVATE)
    @OneToMany(mappedBy = "photo", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<PhotoComment> comments = new ArrayList<>();

    public void addComment(PhotoComment comment) {
      comments.add(comment);
      comment.setPhoto(this);
    }

    public void removeComment(PhotoComment comment) {
        comments.remove(comment);
        comment.setPhoto(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Photo photo = (Photo) o;
        return id.equals(photo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
