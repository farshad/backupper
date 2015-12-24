package xyz.farshad;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.nio.file.StandardWatchEventKinds.*;

public class App {
    public static void main(String[] args) throws Exception {
        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path backupDir = FileSystems.getDefault().getPath("/home/farshad/Public");
        Path filesDir = FileSystems.getDefault().getPath("/home/farshad/Public/shell/ssh");
        filesDir.register(watcher, ENTRY_MODIFY, ENTRY_CREATE, ENTRY_DELETE);
        String zipName;

        Zip zip = new Zip();
        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                if (kind == OVERFLOW || String.valueOf(fileName).equals(".backupper.txt")) {
                    continue;
                } else{
                    System.out.println(kind.name() + ": " + fileName);
                    try {
                        zipName = generateFile(filesDir, backupDir);
                        zip.zipFolder(String.valueOf(filesDir), zipName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            boolean valid = key.reset();
            if (!valid) {
                break;
            }
       }
    }

    private static String generateFile(Path filesDir, Path backupDir) throws IOException {

        Roozh jalali = new Roozh();
        Date date = new Date();
        int[] convertDate = {
                Integer.parseInt(String.format("%tY", date)),
                Integer.parseInt(String.format("%tm", date)),
                Integer.parseInt(String.format("%td", date)),
        };

        //create file if not exist
        Path file = Paths.get(filesDir+"/.backupper.txt");
        File checkFile = new File(String.valueOf(file));

        int currrentFile;
        if(!(checkFile.exists())){
            currrentFile = 0;
        }else {
            FileReader fileReader = new FileReader(String.valueOf(file));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            currrentFile = Integer.parseInt(bufferedReader.readLine());

            if (currrentFile == 10){
                currrentFile = 0;
                System.out.println("ten statement:" + currrentFile);
            }else{
                System.out.println("beforePlus:" + currrentFile);
                currrentFile +=1;
                System.out.println("afterPlus:" + currrentFile);
            }
        }

        // Convert the date
        jalali.GregorianToPersian(convertDate[0], convertDate[1], convertDate[2]);
        String zipName = String.valueOf(backupDir)+"/"+currrentFile+"_bk."+jalali.toString()+" "+new SimpleDateFormat("H:mm:ss").format(date)+".zip";


        List<String> lines = Arrays.asList(""+currrentFile+"");
        Files.write(file, lines, Charset.forName("UTF-8"));
        return zipName;
    }
}