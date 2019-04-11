import java.io.File;
import java.io.IOException;
import freemarker.template.Configuration;
/**
 * freemark配置类
 */
public class FreeConfig {

    private static Configuration config = null;

    public static synchronized Configuration getConfiguation() {
        if (config == null) {
            setConfiguation();
        }
        return config;
    }

    private static void setConfiguation() {
        config = new Configuration();
        String path = ResourceLoader.getPath("");
        System.out.println("path="+path);
        try {
            config.setDirectoryForTemplateLoading(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}