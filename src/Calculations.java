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
        for (long i = 0; i < config.getTotalPages(); i++) {  
            pages.add(new Page(i));
        }
    }
    
    private void calculateResults() {
        long optimal = optimal();    
        long nru = nru();           
        long clock = clock();       
        long wsclock = wsclock();   

        answer = optimal + "\n" + nru + "\n" + clock + "\n" + wsclock;
    }

    private long optimal() {  
        resetState();
        long pageFaults = 0;  
        for (int i = 0; i < accessSequence.size(); i++) {
            Configuration access = accessSequence.get(i);
            Page page = pages.get((int)access.getPageNumber());  

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
        long farthestUse = -1;  
        Page victim = null;
        for (Page frame : frames) {
            long nextUse = findNextUse(frame.getPageNumber(), currentIndex + 1);  
            if (nextUse == -1)
                return frame;
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

    private long findNextUse(long pageNumber, int startIndex) { 
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

    private long clock() {  
        resetState();
        long pageFaults = 0; 
        int clockHand = 0;

        for (Configuration access : accessSequence) {
            Page page = pages.get((int)access.getPageNumber());  

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
                        clockHand = (clockHand + 1) % (int)config.getTotalFrames();  
                    }
                }
            }

            updatePageStatus(page, access.isWrite());
        }

        return pageFaults;
    }

    private long wsclock() {  
        resetState();
        long pageFaults = 0;  
        int clockHand = 0;
        long tau = config.getClockInterval();  
        for (Configuration access : accessSequence) {
            Page page = pages.get((int)access.getPageNumber());  
            
            if (!page.isPresent()) {
                pageFaults++;
                if (frames.size() < config.getTotalFrames()) {
                    frames.add(page);
                    page.setPresent(true);
                } else {
                    boolean victimFound = false;
                    int startHand = clockHand;
                    
                    do {
                        Page current = frames.get(clockHand);
                        long age = access.getAccessTime() - current.getLastAccess();
                        
                        if (age > tau) {
                            if (!current.isReferenced() || !current.isModified()) {
                                current.setPresent(false);
                                frames.set(clockHand, page);
                                page.setPresent(true);
                                victimFound = true;
                                clockHand = (clockHand + 1) % frames.size();
                                break;
                            }
                        }
                        
                        current.setReferenced(false);
                        clockHand = (clockHand + 1) % frames.size();
                    } while (clockHand != startHand && !victimFound);
                    
                    if (!victimFound) {
                        do {
                            Page current = frames.get(clockHand);
                            if (!current.isReferenced()) {
                                current.setPresent(false);
                                frames.set(clockHand, page);
                                page.setPresent(true);
                                victimFound = true;
                                clockHand = (clockHand + 1) % frames.size();
                                break;
                            }
                            current.setReferenced(false);
                            clockHand = (clockHand + 1) % frames.size();
                        } while (clockHand != startHand && !victimFound);
                        
                        if (!victimFound) {
                            Page victim = frames.get(clockHand);
                            victim.setPresent(false);
                            frames.set(clockHand, page);
                            page.setPresent(true);
                            clockHand = (clockHand + 1) % frames.size();
                        }
                    }
                }
            }
            
            updatePageStatus(page, access.isWrite());
            page.setLastAccess(access.getAccessTime());
        }
        return pageFaults;
    }

    private long nru() {  
        resetState();
        long pageFaults = 0;  
        long resetCycles = config.getClockInterval();  
        long currentTime = 0;  

        for (Configuration access : accessSequence) {
            Page page = pages.get((int)access.getPageNumber());  
            currentTime = access.getAccessTime();

            if (!page.isPresent()) {
                pageFaults++;
                if (frames.size() < config.getTotalFrames()) {
                    frames.add(page);
                    page.setPresent(true);
                    updatePageStatus(page, access.isWrite());
                } else {
                    
                    List<Page>[] classes = new ArrayList[4];
                    for (int i = 0; i < 4; i++) {
                        classes[i] = new ArrayList<>();
                    }

                    for (Page frame : frames) {
                        int classNum = (frame.isReferenced() ? 2 : 0) + (frame.isModified() ? 1 : 0);
                        classes[classNum].add(frame);
                    }

                    Page victim = null;
                    for (List<Page> cls : classes) {
                        if (!cls.isEmpty()) {
                            victim = cls.get(0);
                            break;
                        }
                    }


                    frames.remove(victim);
                    victim.setPresent(false);
                    frames.add(page);
                    page.setPresent(true);
                    updatePageStatus(page, access.isWrite());
                }
            } else {
                updatePageStatus(page, access.isWrite());
            }

            if (resetCycles > 0 && currentTime % resetCycles == 0) {
                for (Page frame : frames) {
                    frame.setReferenced(false);
                }
            }
        }
        return pageFaults;
    }
}