import fs from 'fs/promises';
import fetch from 'node-fetch';

(async () => {
    const fd = await fs.open('failRequests.txt', 'a+');
    const fd2 = await fs.open('stationLocation.txt', 'a+');
    let errorCnt = 0;
    let fetchCnt = 0;

    let remainBuf = Buffer.alloc(0);
    let position = 0;
    while (1) {
        const buffer = Buffer.alloc(2048);
        let fdRead = await fd.read({
            buffer: buffer,
            position: position
        });
    
        if (fdRead.bytesRead == 0)
            break;

        position += fdRead.bytesRead;

        remainBuf = Buffer.concat([remainBuf, buffer]);
        const strArr = remainBuf.toString().split(new RegExp('\n'));

        for (let i = 0; i < strArr.length - 1; i++) {
            const url = strArr[i];
            console.log('fetch', fetchCnt);
            try {
                // feth url;
                await new Promise(resolve => setTimeout(resolve, 2000));
                const response = await fetch(url);
                if (response.ok) {
                    const json = await response.json();
                    const items = json.response.body.items;
                    for (let j = 0; j < items.length; j++) {
                        const item = items[j];
                        const locationInfo = item.stationName + ' ' + item.dmX + ' ' + item.dmY;
                        await fd2.writeFile('\n' + locationInfo);
                    }
                }
                else
                    throw new Error('response error');
            }
            catch (error) {
                await fd.writeFile('\n' + url);
                console.log('error', errorCnt)
                console.log(error);
                
                errorCnt += 1;
                if (errorCnt > 300) {
                    fd.close();
                    fd2.close(); 
                    return;
                }
            }
        }

        remainBuf = Buffer.from(strArr[strArr.length - 1]);
        const nullIdx = remainBuf.indexOf('\0');
        if (nullIdx != -1) {
            remainBuf = remainBuf.slice(0, nullIdx);
        }
    }
    
    try {
        // feth url;
        console.log('fetch', fetchCnt);

        await new Promise(resolve => setTimeout(resolve, 2000));
        const response = await fetch(remainBuf.toString());
        if (response.ok) {
            const json = await response.json();
            const items = json.response.body.items;
            for (let j = 0; j < items.length; j++) {
                const item = items[j];
                const locationInfo = item.stationName + ' ' + item.dmX + ' ' + item.dmY;
                await fd2.writeFile('\n' + locationInfo);
            }
        }
        else
            throw new Error('response error');
    }
    catch (error) {
        await fd.writeFile('\n' + remainBuf.toString());

        console.log('error', errorCnt);
        console.log(error);
    }

    fd.close();
    fd2.close(); 
})();