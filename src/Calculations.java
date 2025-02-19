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

    private int wsclock() {
        resetState();
        int pageFaults = 0;
        int clockHand = 0;
        int tau = config.getClockInterval();

        for (Configuration access : accessSequence) {
            Page page = pages.get(access.getPageNumber());
            
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
                        int age = access.getAccessTime() - current.getLastAccess();
                        
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



    private int nru() {
        resetState();
        int pageFaults = 0;
        
        for (Configuration access : accessSequence) {
            // Reset reference bits at clock intervals
            if (access.getAccessTime() > 0 && access.getAccessTime() % config.getClockInterval() == 0) {
                for (Page p : pages) {
                    if (p.isPresent()) {
                        p.setReferenced(false);
                    }
                }
            }

            Page currentPage = pages.get(access.getPageNumber());

            if (!currentPage.isPresent()) {
                pageFaults++;

                if (frames.size() < config.getTotalFrames()) {
                    frames.add(currentPage);
                    currentPage.setPresent(true);
                } else {
                    // Classify pages into classes
                    ArrayList<ArrayList<Page>> classes = new ArrayList<>();
                    for (int i = 0; i < 4; i++) {
                        classes.add(new ArrayList<>());
                    }

                    for (Page page : frames) {
                        int classIndex = (page.isReferenced() ? 2 : 0) + (page.isModified() ? 1 : 0);
                        classes.get(classIndex).add(page);
                    }

                    // Find victim from lowest non-empty class
                    Page victim = null;
                    for (ArrayList<Page> classPages : classes) {
                        if (!classPages.isEmpty()) {
                            victim = classPages.get(0);
                            break;
                        }
                    }

                    frames.remove(victim);
                    victim.setPresent(false);
                    frames.add(currentPage);
                    currentPage.setPresent(true);
                }
            }

            // Update page status
            currentPage.setReferenced(true);
            if (access.isWrite()) {
                currentPage.setModified(true);
            }
        }

        return pageFaults;
    }
}