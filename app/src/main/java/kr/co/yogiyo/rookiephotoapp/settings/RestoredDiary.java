package kr.co.yogiyo.rookiephotoapp.settings;

import com.google.gson.annotations.SerializedName;

public class RestoredDiary {

    @SerializedName("id")
    private Integer id;
    @SerializedName("diary_id")
    private Integer diaryId;
    @SerializedName("date")
    private String date;
    @SerializedName("description")
    private String description;
    @SerializedName("image")
    private String image;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("diary_user")
    private Integer diaryUser;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDiaryId() {
        return diaryId;
    }

    public void setDiaryId(Integer diaryId) {
        this.diaryId = diaryId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getDiaryUser() {
        return diaryUser;
    }

    public void setDiaryUser(Integer diaryUser) {
        this.diaryUser = diaryUser;
    }
}
