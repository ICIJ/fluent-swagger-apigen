package org.icij.swagger.petstore;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.ArrayList;

import javax.xml.bind.annotation.*;

@XmlRootElement(name = "Pet")
public class Pet {
    private long id;
    private Category category;
    private String name;
    private List<String> photoUrls = new ArrayList<String>();
    private List<Tag> tags = new ArrayList<Tag>();
    private String status;

    @XmlElement(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @XmlElement(name = "category")
    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @XmlElement(name = "name")
    @Schema(example = "doggie", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElementWrapper(name = "photoUrls")
    @XmlElement(name = "photoUrl", required = true)
    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    @XmlElementWrapper(name = "tags")
    @XmlElement(name = "tag")
    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }

    @XmlElement(name = "status")
    @Schema(name = "status", description = "pet status in the store", allowableValues = "available,pending,sold", example = "available")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}