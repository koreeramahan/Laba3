package MyPackage;

import java.util.Hashtable;

public class UrlContainer
{
    private Hashtable<String, UrlInfo> notvisitedUrl = new Hashtable<>();
    private Hashtable<String, UrlInfo> visitedUrl = new Hashtable<>();

    public UrlContainer(String firstUrl)
    {
        UrlInfo urlInfo = new UrlInfo(firstUrl, 0);
        String key = urlInfo.getDomain()+urlInfo.getPath()+urlInfo.getParams();
        notvisitedUrl.put(key, urlInfo);
    }

    public synchronized UrlInfo getNotVisitedUrl()
    {
        System.out.println(notvisitedUrl.size());
        while (notvisitedUrl.size() == 0)
        {
            try
            {
                wait(); //бесконечно ждет другой поток, пока не будет вызван метод notify() или notifyAll() на объекте
            }
            catch (InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
        }
        String key = notvisitedUrl.keys().nextElement();
        UrlInfo urlInfo = notvisitedUrl.get(key);
        notvisitedUrl.remove(key);
        return urlInfo;
    }

    public synchronized void setNotVisitedUrl(UrlInfo urlInfo)
    {
        String key = urlInfo.getDomain()+urlInfo.getPath()+urlInfo.getParams();
        if (!visitedUrl.containsKey(key) && !notvisitedUrl.containsKey(key))
        {
            notvisitedUrl.put(key, urlInfo);
        }
        notify(); //Вызов метод notify() пробуждает только один поток, после чего этот поток начинает выполнение.
        // Если объект ожидают несколько потоков, то метод notify() разбудит только один из них.
    }

    public Hashtable<String, UrlInfo> getVisitedUrl()
    {
        return visitedUrl;
    }

    public synchronized void setVisitedUrl(UrlInfo urlInfo)
    {
        String key = urlInfo.getDomain()+urlInfo.getPath()+urlInfo.getParams();
        if (!visitedUrl.containsKey(key))
        {
            visitedUrl.put(key, urlInfo);
        }
    }
}
