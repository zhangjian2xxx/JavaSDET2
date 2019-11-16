package app.page;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.appium.java_client.MobileBy;
import io.appium.java_client.TouchAction;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.touch.offset.PointOption;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class BasePage {
    public static AndroidDriver<WebElement> driver;

    public static WebElement findElement(By by) {
        //todo: 递归是更好的
        //todo: 如果定位的元素是动态变化位置

        System.out.println(by);
        try {
            return driver.findElement(by);
        } catch (Exception e) {
            handleAlert();

            return driver.findElement(by);
        }
    }

    public static void click(By by) {
        //todo: 递归是更好的

        System.out.println(by);
        try {
            driver.findElement(by).click();
        } catch (Exception e) {
            handleAlert();

            driver.findElement(by).click();
        }
    }

    public static List<WebElement> findElements(By by) {
        System.out.println(by);
        return driver.findElements(by);
    }

    static void handleAlert() {
        By tips = By.id("com.xueqiu.android:id/snb_tip_text");
        List<By> alertBoxs = new ArrayList<>();
        //todo: 不需要所有的都判断是否存在
        alertBoxs.add(By.id("com.xueqiu.android:id/image_cancel"));
        alertBoxs.add(tips);
        alertBoxs.add(By.id("com.xueqiu.android:id/md_buttonDefaultNegative"));
//        alertBoxs.add(By.xpath("dddd"));

        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        alertBoxs.forEach(alert -> {
            List<WebElement> ads = driver.findElements(alert);

            if (alert.equals(tips)) {
                System.out.println("snb_tip found");
                Dimension size = driver.manage().window().getSize();
                try {
                    if (driver.findElements(tips).size() >= 1) {
                        new TouchAction(driver).tap(PointOption.point(size.width / 2, size.height / 2)).perform();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("snb_tip clicked");
                }
            } else if (ads.size() >= 1) {
                ads.get(0).click();
            }
        });
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }

    private static void handleAlertByPageSource() {
        //todo: xpath匹配， 标记 定位
        String xml = driver.getPageSource();
        List<String> alertBoxs = new ArrayList<>();
        alertBoxs.add("xxx");
        alertBoxs.add("yyy");

        alertBoxs.forEach(alert -> {
            if (xml.contains(alert)) {
                driver.findElement(By.id(alert)).click();
            }
        });

    }

    public void parseSteps(String method) {
//        HashMap<String, List<HashMap<String, String>>> 可以取消steps的多余关键字
        //TODO: 参数化，把关键数据参数化到你的yaml中
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String path = "/" + this.getClass().getCanonicalName().replace('.', '/') + ".yaml";
        TypeReference<HashMap<String, TestCaseSteps>> typeRef = new TypeReference<HashMap<String, TestCaseSteps>>() {
        };
        try {
            HashMap<String, TestCaseSteps> steps = mapper.readValue(
                    this.getClass().getResourceAsStream(path), typeRef
            );
            parseSteps(steps.get(method));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void parseSteps(String path, String method){
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TypeReference<HashMap<String, TestCaseSteps>> typeRef = new TypeReference<HashMap<String, TestCaseSteps>>() {
        };
        try {
            HashMap<String, TestCaseSteps> steps = mapper.readValue(
                    BasePage.class.getResourceAsStream(path), typeRef
            );
            parseSteps(steps.get(method));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private static void parseSteps(TestCaseSteps steps){
        steps.getSteps().forEach(step->{
            WebElement element = null;

            //todo: 多个可能定位，多平台支持，多版本的支持
            String id=step.get("id");
            if(id!=null){
                element=driver.findElement(By.id(id));
            }

            String xpath=step.get("xpath");
            if(xpath!=null){
                element=driver.findElement(By.xpath(xpath));
            }

            String aid=step.get("aid");
            if(aid!=null){
                element=driver.findElement(MobileBy.AccessibilityId(aid));
            }

            String send=step.get("send");
//            send.replaceAll("{.*}", "dd")
            if(send!=null){
                element.sendKeys(send);
            }else if(step.get("get")!=null){
                element.getAttribute(step.get("get"));
            }else{
                element.click();
            }

        });
    }


}
