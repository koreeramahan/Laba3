import java.util.Enumeration; //для перебора элементов
import java.util.Hashtable; //для хранения ключ/значение в хэш-таблице
import java.util.ArrayList;

import MyPackage.*;

public class Main1
{
    public static void main(String[] args) {
        //три аргумента
        if (args.length != 3)
        {
            System.out.println("usage: Laba3 <URL> <depth> <threads count>");
            return;
        }
        //в контейнер - страницу, затем глубина и число потоков
        UrlContainer urlContainer = new UrlContainer(args[0]);
        int depth = Integer.parseInt(args[1]);
        int thread = Integer.parseInt(args[2]);
        ArrayList<UrlScanner> scanners = new ArrayList<>(thread);
        for (int i = 0; i < thread; i++)
        {
            UrlScanner scanner = new UrlScanner(urlContainer, depth);
            scanner.setDaemon(true); //поток для фоновых действий по обслуживанию основных потоков
            scanner.start();
            scanners.add(scanner);
        }
        boolean process = true;
        while (process == true)
        {
            Thread.yield(); //возвращает выполняющийся поток в состоянии runnable, для того чтобы уступить место другому потоку
            process = false;
            //пока поток не в ожидании, выполняем процесс
            for (UrlScanner scanner : scanners)
            {
                if (scanner.getState() != Thread.State.WAITING)
                {
                    process = true;
                    break;
                }
            }
        }
        //помещаем в хэштаблицу просмотренные страницы
        Hashtable<String, UrlInfo> visitedUrl = urlContainer.getVisitedUrl();
        //для информации о оставшихся элементах
        Enumeration<UrlInfo> val = visitedUrl.elements();
        //просматриваем элементы пока они есть
        while (val.hasMoreElements())
        {
            UrlInfo urlInfo = val.nextElement();
            System.out.println(urlInfo.getUrl());
        }
        System.out.println("Всего просмотрено url: " + Integer.toString(visitedUrl.size()));
    }
}

