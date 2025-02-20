const fs = require('fs');

class Page {
  constructor(number) {
    this.number = number;
    this.referenced = false;
    this.modified = false;
    this.present = false;
  }
}

function nru(input) {

  const lines = input.trim().split('\n');
 
  const totalPages = parseInt(lines[0]);
  const totalFrames = parseInt(lines[1]);
  const clockInterval = parseInt(lines[2]);
  

  const pages = Array.from({ length: totalPages }, (_, i) => new Page(i));
  const frames = [];
  let pageFaults = 0;

  const sequence = lines.slice(3).map(line => {
    const [page, time, op] = line.split(' ');
    return {
      page: parseInt(page),
      time: parseInt(time),
      write: op.trim() === 'W'
    };
  });

  for (const access of sequence) {
    if (access.time > 0 && access.time % clockInterval === 0) {
      for (const page of pages) {
        if (page.present) {
          page.referenced = false;
        }
      }
    }

    const currentPage = pages[access.page];

    if (!currentPage.present) {
      pageFaults++;

      if (frames.length < totalFrames) {
        frames.push(currentPage);
        currentPage.present = true;
      } else {
        const classes = [[], [], [], []];
        for (const page of frames) {
          const classIndex = (page.referenced ? 2 : 0) + (page.modified ? 1 : 0);
          classes[classIndex].push(page);
        }

        let victim = null;
        for (const classPages of classes) {
          if (classPages.length > 0) {
            victim = classPages[0];
            break;
          }
        }

        const victimIndex = frames.indexOf(victim);
        victim.present = false;
        frames[victimIndex] = currentPage;
        currentPage.present = true;
      }
    }
    currentPage.referenced = true;
    if (access.write) {
      currentPage.modified = true;
    }
  }

  return pageFaults;
}

try {
  const input = fs.readFileSync('a.txt', 'utf8');
  console.log(nru(input));
} catch (err) {
  console.error('Error reading file:', err);
}