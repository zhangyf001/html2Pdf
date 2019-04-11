import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.w3c.dom.Document;
import org.xhtmlrenderer.pdf.ITextRenderer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PdfDocumentGenerator {
    public static void main(String[] args) {
        String htmlContent = "<html>\n" +
                "<head>\n" +
                "    <style type=\"text/css\">\n" +
                "        body {\n" +
                "            margin-left: 45px;\n" +
                "            margin-right: 45px;\n" +
                "            font-family: Arial Unicode MS;/*必须要有这个，不然中文无法显示*/\n" +
                "            font-size: 10px;\n" +
                "        }\n" +
                "    </style>\n" +
                "   \n" +
                "</head>\n" +
                "<body>\n" +
                "<h2>Hello World!</h2>\n" +
                "伟大的中国!为\n" +
                "便于约定，保险种种总以中英文写成，如保微微险单中中英文表达，请以中国文为准。文表达，请以中国文为准文表达，请以中国文为准文表达，请\n" +
                "以中国文为准文表达，请以中国文为准文表达，请以中国文为准文表达，请以中国文为准！\n" +
                "</body>\n" +
                "</html>\n";
        try {
            generate(htmlContent,"D://pdfFile/test.pdf");

            List fileList = new ArrayList();
            fileList.add("D://pdfFile/test1.pdf");
            fileList.add("D://pdfFile/test2.pdf");
            fileList.add("D://pdfFile/test3.pdf");
           morePdfTopdf(fileList,"D://pdfFile/testAll.pdf");
           addPageCode(null,"D://pdfFile/testAll.pdf","D://pdfFile/testAll_new.pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * 生成PDF
     * @param htmlContent html内容
     * @param outputFile  生成PDF的存放路径
     * @throws Exception
     */
    public static void generate(String htmlContent, String outputFile)
            throws Exception {
        OutputStream out = null;
        ITextRenderer iTextRenderer = new ITextRenderer();
        try {
            DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(htmlContent.getBytes(StandardCharsets.UTF_8)));
            File f = new File(outputFile);
            if (f != null && !f.getParentFile().exists()) {
                f.getParentFile().mkdir();
            }
            out = new FileOutputStream(outputFile);

            iTextRenderer = (ITextRenderer) ITextRendererObjectFactory
                    .getObjectPool().borrowObject();//获取对象池中对象

            try {
                iTextRenderer.setDocument(doc, null);
                iTextRenderer.layout();
                iTextRenderer.createPDF(out);
            } catch (Exception e) {
                ITextRendererObjectFactory.getObjectPool().invalidateObject(
                        iTextRenderer);
                iTextRenderer = null;
                throw e;
            }

        } catch (Exception e) {
            throw e;
        } finally {
            if (out != null) {
                out.close();
            }

            if (iTextRenderer != null) {
                try {
                    ITextRendererObjectFactory.getObjectPool().returnObject(iTextRenderer);
                } catch (Exception ex) {
                    System.out.println("Cannot return object from pool."+ ex);
                }
            }
        }
    }

    /**
     * 添加页码、页眉、页脚
     */
    public static void addPageCode(Map map , String oldPdfPath, String newPdfPath){
       try {

           //读取文件 --生成好的pdf文件
           PdfReader pdfReader = new PdfReader(oldPdfPath);

           //生成文件--输出的pdf文件
           PdfStamper pdfStamper = new PdfStamper(pdfReader, new FileOutputStream(newPdfPath));
           //添加页码
           for(int i=1; i<= pdfReader.getNumberOfPages(); i++) {
               PdfContentByte content = pdfStamper.getUnderContent(i);
               content.setLineWidth(1f);
               content.moveTo(45,36);
               content.lineTo(555,36);
               content.stroke();

               content.setLineWidth(1f);
               content.moveTo(45,818);
               content.lineTo(555,818);
               content.stroke();
               //添加文字
               BaseFont font = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
               content.beginText();
               content.setFontAndSize(font, 13);
               content.setTextMatrix(200, 200);// 200 200 页脚
               content.showTextAligned(Element.CCITT_ENDOFBLOCK,"德玛西亚万岁",220,820,0);//左边距、下边距 设置页眉
               content.showTextAligned(Element.CCITT_ENDOFBLOCK,"   第   " + i + "  页/共   " + pdfReader.getNumberOfPages() + "页",150,20,0);//左边距、下边距设置页脚

               content.endText();
           }
           pdfStamper.close();
       }catch(Exception e){
           e.printStackTrace();
       }
    }

    /**
     * 多个PDF合并成一个
     * @param fileList  要合并的多个pdf文件
     * @param savepath  合并完成后的PDF文件
     */
    public static void morePdfTopdf(List<String> fileList, String savepath) {
        com.itextpdf.text.Document document = null;
        try {
            document = new com.itextpdf.text.Document(new PdfReader(fileList.get(0)).getPageSize(1));
            PdfCopy copy = new PdfCopy(document, new FileOutputStream(savepath));
            document.open();
            for (int i = 0; i < fileList.size(); i++) {
                PdfReader reader = new PdfReader(fileList.get(i));
                int n = reader.getNumberOfPages();// 获得总页码
                for (int j = 1; j <= n; j++) {
                    document.newPage();
                    PdfImportedPage page = copy.getImportedPage(reader, j);// 从当前Pdf,获取第j页
                    copy.addPage(page);
                }
                System.out.println(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (document != null) {
                document.close();
            }
            System.out.println("finish " + new Date());
        }
    }

    /**
     * 替换模板数据,返回html
     * @param template 模板（要遵循freemaker语法）
     * @param map 数据
     * @return
     * @throws IOException
     * @throws TemplateException
     */

    public String replaceTemp(String template, Map<String,Object> map) throws IOException, TemplateException {
        template = "config/templates/myTemplate.html";
        BufferedWriter writer = null;
        String htmlContent = "";
        try{
            Configuration config = FreeConfig.getConfiguation();
            Template tp = config.getTemplate(template);
            StringWriter stringWriter = new StringWriter();
            writer = new BufferedWriter(stringWriter);

            tp.setEncoding("UTF-8");
            tp.process(map, writer);
            htmlContent = stringWriter.toString();
            writer.flush();

        }finally{
            if(writer!=null)
                writer.close();
        }
        return htmlContent;
    }
}
