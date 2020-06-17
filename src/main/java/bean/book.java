package bean;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public class book {

    @JsonProperty("id")
    private int id;

    @JsonProperty("Title")
    @JsonAlias({"title"})
    private String Title;

    @JsonProperty("Author")
    @JsonAlias({"author"})
    private String Author;

    @JsonProperty("Publisher")
    @JsonAlias({"publisher"})
    private String Publisher;

    @JsonProperty("Year")
    @JsonAlias({"year"})
    private int Year;

    @JsonProperty("Available")
    @JsonAlias({"available"})
    private boolean Available;

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("sortby")
    private String sortby;

    @JsonProperty("order")
    private String order;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getPublisher() {
        return Publisher;
    }

    public void setPublisher(String publisher) {
        Publisher = publisher;
    }

    public int getYear() {
        return Year;
    }

    public void setYear(int year) {
        Year = year;
    }

    public boolean isAvailable() {
        return Available;
    }

    public void setAvailable(boolean available) {
        Available = available;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSortby() {
        return sortby;
    }

    public void setSortby(String sortby) {
        this.sortby = sortby;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
