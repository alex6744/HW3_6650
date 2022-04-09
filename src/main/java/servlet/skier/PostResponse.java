package servlet.skier;

public class PostResponse {
  private int resortId;
  private String season;
  private String day;
  private int skierId;

  public PostResponse(int resortId, String season, String day, int skierId) {
    this.resortId = resortId;
    this.season = season;
    this.day = day;
    this.skierId = skierId;
  }

  @Override
  public String toString() {
    return
         resortId +
         ","+ season +
        ","+ day +
        "," + skierId
        ;
  }
}
