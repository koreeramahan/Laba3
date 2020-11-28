package MyPackage;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlScanner extends Thread
{
    private final static String PATTERN = "href=\"http://.+?\"";
    private static UrlContainer urlContainer;
    private static int m_maxDepth = 1;

    public UrlScanner(UrlContainer container, int maxDepth)
    {
        urlContainer = container;
        m_maxDepth = maxDepth;
    }

    @Override
    public void run()
    {
        while (true)
        {
            UrlInfo urlInfo = urlContainer.getNotVisitedUrl();
            scanUrl(urlInfo);
            urlContainer.setVisitedUrl(urlInfo);
        }
    }

    public static void scanUrl(UrlInfo url)
    {
        try
        {
            String ip = InetAddress.getByName(url.getDomain()).toString();
            ip = ip.split("/")[1];
            Socket socket = new Socket(ip, 80);
            socket.setSoTimeout(5000);
            PrintWriter p = new PrintWriter(socket.getOutputStream()); //печать форматированных представлений в поток текста
            p.println("GET " + url.getPath() + " HTTP/1.1");
            p.println("Host: " + url.getDomain());
            p.println("");
            p.flush(); //очищаем поток
            BufferedReader b = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String buff = b.readLine();
            if (buff.contains("200"))
            {
                for (buff = b.readLine(); buff != null && !buff.equals("</html>"); buff = b.readLine())
                {
                    Pattern pattern = Pattern.compile(PATTERN); //создаем объект класса, в основе pattern
                    Matcher matcher = pattern.matcher(buff); //хранит согласования
                    //находим все совпадения
                    while (matcher.find())
                    {
                        String htmlUrl = buff.substring(matcher.start(), matcher.end());
                        int depth = url.getDepth();
                        //прибавляем глубину
                        UrlInfo urlInfo = new UrlInfo(htmlUrl.substring(6, htmlUrl.length()-1), depth+1);
                        //если глубина не совпадает с максимальной, продолжаем сканирование
                        if (depth+1 <= m_maxDepth)
                        {
                            urlContainer.setNotVisitedUrl(urlInfo);
                        }
                    }
                }
            }
            socket.close();
        }
        catch (Exception ex)
        {

        }
    }
}

