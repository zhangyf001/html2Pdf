import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.File;
import java.io.IOException;

/**
 * ITextRenderer对象工厂,提供性能,加载中文字体集(大小20M),故增加对象池
 */
public class ITextRendererObjectFactory extends
        BasePooledObjectFactory<ITextRenderer> {
    private final static GenericObjectPool itextRendererObjectPool = init();

    @Override
    public ITextRenderer create() throws Exception {
        ITextRenderer renderer = createTextRenderer();
        return renderer;
    }

    @Override
    public PooledObject wrap(ITextRenderer obj) {
        return new DefaultPooledObject(obj);
    }

    /**
     * 获取对象池,使用对象工厂 后提供性能,能够支持 500线程 迭代10
     */
    public static GenericObjectPool getObjectPool() {
        return itextRendererObjectPool;
    }

    private static GenericObjectPool init() {
        GenericObjectPool itextRendererObjectPool = new GenericObjectPool(
                new ITextRendererObjectFactory());
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setLifo(false);
        config.setMaxTotal(15);
        config.setMaxIdle(5);
        config.setMinIdle(1);
        config.setMaxWaitMillis(5 * 1000);
        itextRendererObjectPool.setConfig(config);
        return itextRendererObjectPool;
    }

    /**
     * 初始化
     */
    public static synchronized ITextRenderer createTextRenderer()
            throws DocumentException, IOException {
        ITextRenderer renderer = new ITextRenderer();
        ITextFontResolver fontResolver = renderer.getFontResolver();
        addFonts(fontResolver);
        return renderer;
    }

    /**
     * 添加字体
     */
    public static ITextFontResolver addFonts(ITextFontResolver fontResolver)
            throws DocumentException, IOException {
        // Font fontChinese = null;
        // BaseFont bfChinese = BaseFont.createFont("STSong-Light",
        // "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
        // fontChinese = new Font(bfChinese, 12, Font.NORMAL);

        File fontsDir = new File(ResourceLoader.getPath("config/fonts"));
        if (fontsDir != null && fontsDir.isDirectory()) {
            File[] files = fontsDir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                if (f == null || f.isDirectory()) {
                    break;
                }
                fontResolver.addFont(f.getAbsolutePath(), BaseFont.IDENTITY_H,
                        BaseFont.NOT_EMBEDDED);
            }
        }
        return fontResolver;
    }
}
