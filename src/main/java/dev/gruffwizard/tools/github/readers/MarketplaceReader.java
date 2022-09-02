package dev.gruffwizard.tools.github.readers;

import dev.gruffwizard.tools.github.cmd.MarketplaceCommand;
import org.jboss.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import picocli.CommandLine;

import java.io.*;
import java.util.LinkedList;
import java.util.List;



public class MarketplaceReader {

    public static final String INSTALLS = " installs";
    public static final String STARS = " stars";

    private static final Logger LOG = Logger.getLogger(MarketplaceReader.class);

    public static Runner runner() {
        return new Runner();
    }

    private static class Entry {

        String href="";
        String title="";
        String description="";
        String provider="";
        String type="";
        String references="n/a";

    }

    private static void adjustRecommendations(List<Entry> entries) {

        Double last=findEntry(entries,0);

      for(int i=0;i<entries.size();i++) {
         Entry e=entries.get(i);
         if(e.references.toLowerCase().trim().equals("recommended")) {
            Double fullEntryAfter=findEntry(entries,i);
            last=(last+fullEntryAfter)/2f;
            e.references=""+last;
         } else {
             last=Double.parseDouble(e.references);
         }
      }

    }

    private static Double findEntry(List<Entry> entries, int i) {
        for(;i<entries.size();i++) {
            Entry n=entries.get(i);
            if(n.references.toLowerCase().trim().equals("recommended")==false) {
                return Double.parseDouble(n.references);
            }
        }
        return null;
    }

    private static void printList(File out,List<Entry> entries) throws IOException {

        PrintWriter pw=new PrintWriter(out);
        pw.println("type,url,name,title,uses,description");
        for(var a: entries) {
            long l= (long) Double.parseDouble(a.references);

            pw.println(escape(a.type)+","
                      +escape(a.href)+","
                      +escape(a.title) + ","
                      +escape(a.provider) + ","
                      +l + ","
                      +escape(a.description));
        }
        pw.close();
    }

    private static void addData(String type,List<Entry> entries) {
        int page=1;
        int count=0;

        do {
            try {
                count=addEntries(page,type,entries);
            } catch (IOException e) {
                count=0;
            }

            page++;

        } while(count>0);
    }

    private static String escape(String in) {
        return in.replace(","," ");
    }

    private static int addEntries(int page, String type,List<Entry> entries) throws IOException {

        LOG.info("Getting page "+page+" of "+type);

        Document doc = Jsoup.connect("https://github.com/marketplace?page="+page+"&q=sort%3Apopularity-desc&query=sort%3Apopularity-desc&type="+type).get();

        int count=0;

        Elements apps=doc.select("h3");

        for(Element e:apps) {
            String title=e.text();
            if(title.toLowerCase().trim().equals(type)) {
                for(Element app:e.parent().select("a .px-3")) {
                    Entry details=readApp(app);
                    if(details!=null) {
                        details.type=type;
                        entries.add(details);
                        count++;
                    } else {
                        LOG.warn("no more entries");
                    }
                }
            }
        }
        return count;
    }

    private static Entry readApp(Element app) {


        int kids=app.childrenSize();

        Entry r=null;


        if(kids>2) {
            r=new Entry();

            if(app.parent().hasAttr("href")) {
                r.href="https://github.com"+app.parent().attr("href");
            }

            r.title = app.child(0).text();
            r.provider = app.child(1).text();
            if(r.provider.startsWith("By ")) r.provider=r.provider.substring(3);
            r.description = app.child(2).text();
            r.references = "0";

            if(kids>3) {
                r.references= app.child(3).text();
            }
            if(kids>4) {
                r.references= app.child(4).text();
            }

            r.references=r.references.trim();
            if(r.references.endsWith(INSTALLS)) {
                r.references=r.references.substring(0,r.references.length()-INSTALLS.length());
            }
            if(r.references.endsWith(STARS)) {
                r.references=r.references.substring(0,r.references.length()-STARS.length());
            }
            if(r.references.endsWith("k")) {
               String size=r.references.substring(0,r.references.length()-1);
               Double q=Double.parseDouble(size);
               q=q*1000;
               r.references=""+q;
            } else  if(r.references.endsWith("m")) {
                String size=r.references.substring(0,r.references.length()-1);
                Double q=Double.parseDouble(size);
                q=q*1000*1000;
                r.references=""+q;
            }
        } else {
            System.out.println("?"+kids+" "+app.text());
        }

        return r;

    }

  public static class Runner {

        boolean apps=false;
        boolean actions=false;
        File output=new File("marketplace.csv");

        public Runner includeApps(boolean apps) {
            this.apps=apps;
            return this;
        }

      public Runner includeActions(boolean actions) {
          this.actions=actions;
          return this;
      }

      public Runner  output(File file) {
            if(file!=null) {
                output=file;
            }
            return this;
      }

      public void execute() throws IOException {

          List<Entry> entries=new LinkedList<>();
          if(apps) addData("apps",entries);
          if(actions) addData("actions",entries);

          adjustRecommendations(entries);

          printList(output,entries);

      }

  }
}
