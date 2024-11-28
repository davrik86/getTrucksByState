package Pages;

import Utils.Driver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;



public class Page {

    public  Page(){
        PageFactory.initElements(Driver.getDriver(), this);
    }

    @FindBy(id = "email")
    public WebElement email;

    @FindBy(id = "_continue")
    public WebElement continiuBTN;

    @FindBy(id = "password")
    public WebElement password;

    @FindBy(id="_submit")
    public WebElement submitBTN;

    @FindBy(xpath="//div[@class='calendar-box-content']")
    public WebElement calendarBTN;

    @FindBy(xpath="//div[@class='calendar cal-bg-27']")
    public WebElement coveredTruckBTN;

    @FindBy(xpath="//div[@class='calendar cal-bg-7']")
    public WebElement maintenanceBttn;

    @FindBy(xpath="(//div[text()='ON HOLD'])[1]")
    public WebElement onHold;

    @FindBy(xpath="(//div[text()='PARTIAL'])[1]")
    public WebElement partial;

    @FindBy(xpath="//div[@class='toolbar-button next']")
    public WebElement nextDayBTN;










}
