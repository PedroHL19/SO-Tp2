import java.io.*;
import java.util.*;

public class Calculations {
    private Configuration config;
    private ArrayList<Page> pages;
    private ArrayList<Page> frames;
    private ArrayList<Configuration> accessSequence;
    private String answer;

    public Calculations() {
        pages = new ArrayList<>();
        frames = new ArrayList<>();
        answer = "";
    }

    public void setConfig(Configuration config, ArrayList<Configuration> accessSequence) {
        this.config = config;
        this.accessSequence = accessSequence;
        initializePages();
        calculateResults();
    }

    private void initializePages() {
        pages.clear();
        for (int i = 0; i < config.getTotalPages(); i++) {
            pages.add(new Page(i));
        }
    }

    private void calculateResults() {
        int optimal = optimal();
        int nru = nru();
        int clock = clock();
        int wsclock = wsclock();
        
        answer = optimal + "\n" + nru + "\n" + clock + "\n" + wsclock;
    }

    private int optimal() {
        resetState();
        int pageFaults = 0;
        for (int i = 0; i < accessSequence.size(); i++) {
            Configuration access = accessSequence.get(i);
            Page page = pages.get(access.getPageNumber());
            
            if (!page.isPresent()) {
                pageFaults++;
                if (frames.size() < config.getTotalFrames()) {
                    frames.add(page);
                } else {
                    Page toRemove = findOptimalVictim(i);
                    frames.remove(toRemove);
                    toRemove.setPresent(false);
                    frames.add(page);
                }
                page.setPresent(true);
            }
            updatePageStatus(page, access.isWrite());
        }
        return pageFaults;
    }

    private Page findOptimalVictim(int currentIndex) {
        int farthestUse = -1;
        Page victim = null;
        for (Page frame : frames) {
            int nextUse = findNextUse(frame.getPageNumber(), currentIndex + 1);
            if (nextUse == -1) return frame;
            if (nextUse > farthestUse) {
                farthestUse = nextUse;
                victim = frame;
            }
        }
        return victim;
    }
    
    
    private void resetState() {
        frames.clear();
        for (Page page : pages) {
            page.setPresent(false);
            page.setReferenced(false);
            page.setModified(false);
            page.setLastAccess(0);
        }
    }

    

    private void updatePageStatus(Page page, boolean isWrite) {
        page.setReferenced(true);
        if (isWrite) {
            page.setModified(true);
        }
    }

    

    private int findNextUse(int pageNumber, int startIndex) {
        for (int i = startIndex; i < accessSequence.size(); i++) {
            if (accessSequence.get(i).getPageNumber() == pageNumber) {
                return i;
            }
        }
        return -1;
    }

    public void write(BufferedWriter writer) throws IOException {
        writer.write(answer);
        writer.newLine();
        writer.flush();
    }
    private int clock() {
        resetState();
        int pageFaults = 0;
        int clockHand = 0;

        for (Configuration access : accessSequence) {
            Page page = pages.get(access.getPageNumber());
            
            if (!page.isPresent()) {
                pageFaults++;
                
                if (frames.size() < config.getTotalFrames()) {
                    frames.add(page);
                    page.setPresent(true);
                } else {
                    while (true) {
                        Page framePage = frames.get(clockHand);
                        if (!framePage.isReferenced()) {
                            framePage.setPresent(false);
                            frames.set(clockHand, page);
                            page.setPresent(true);
                            break;
                        }
                        framePage.setReferenced(false);
                        clockHand = (clockHand + 1) % config.getTotalFrames();
                    }
                }
            }

            updatePageStatus(page, access.isWrite());
        }
        
        return pageFaults;
    }
    
    private int nru() {
        resetState();
        int pageFaults = 0;
        int clockTick = 0;
    
        for (Configuration access : accessSequence) {
            
            if (clockTick % config.getClockInterval() == 0) {
                for (Page p : frames) {
                    p.setReferenced(false);
                }
            }
    
            Page page = pages.get(access.getPageNumber());
            if (!page.isPresent()) {
                pageFaults++;
                if (frames.size() < config.getTotalFrames()) {
                    frames.add(page);
                } else {
                    Page victim = findNRUVictim();
                    frames.remove(victim);
                    victim.setPresent(false);
                    frames.add(page);
                }
                page.setPresent(true);
            }
    
            updatePageStatus(page, access.isWrite());
            clockTick++;
        }
        return pageFaults;
    }
    
    private Page findNRUVictim() {
        List<Page> candidates = new ArrayList<>();
        int minCategory = 4;
        
        for (Page page : frames) {
            int category = (page.isReferenced() ? 2 : 0) + (page.isModified() ? 1 : 0);
            if (category < minCategory) {
                minCategory = category;
                candidates.clear();
            }
            if (category == minCategory) {
                candidates.add(page);
            }
        }
        Page victim = candidates.get(new Random().nextInt(candidates.size()));
        System.out.println("NRU: Substituindo página " + victim.getPageNumber());
        return victim;
    }
    
    private int wsclock() {
        resetState();
        int pageFaults = 0;
        int clockHand = 0;
        int tau = config.getClockInterval() * 2;
    
        for (Configuration access : accessSequence) {
            System.out.println("WSClock: Acessando página " + access.getPageNumber());
            Page page = pages.get(access.getPageNumber());
            if (!page.isPresent()) {
                pageFaults++;
                System.out.println("WSClock: Falta de página detectada!");
                if (frames.size() < config.getTotalFrames()) {
                    frames.add(page);
                } else {
                    boolean replaced = false;
                    int scanCount = 0;
                    
                    while (scanCount < frames.size()) {
                        Page candidate = frames.get(clockHand);
                        System.out.println("WSClock: Analisando página " + candidate.getPageNumber() + " (Referenciado: " + candidate.isReferenced() + ", Último acesso: " + candidate.getLastAccess() + ")");
                        if (!candidate.isReferenced() && access.getAccessTime() - candidate.getLastAccess() > tau) {
                            System.out.println("WSClock: Substituindo página " + candidate.getPageNumber());
                            candidate.setPresent(false);
                            frames.set(clockHand, page);
                            replaced = true;
                            break;
                        }
                        
                        candidate.setReferenced(false);
                        clockHand = (clockHand + 1) % frames.size();
                        scanCount++;
                    }
                    
                    if (!replaced) {
                        Page victim = frames.get(clockHand);
                        System.out.println("WSClock: Nenhum candidato ideal encontrado, substituindo " + victim.getPageNumber());
                        victim.setPresent(false);
                        frames.set(clockHand, page);
                    }
                }
                page.setPresent(true);
            }
            updatePageStatus(page, access.isWrite());
            page.setLastAccess(access.getAccessTime());
            System.out.println("WSClock: Estado atualizado para página " + access.getPageNumber());
        }
        return pageFaults;
    }
    
}
