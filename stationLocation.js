import fetch from 'node-fetch';
import fs from 'fs';
import csv from 'csv-parser';
import { XMLHttpRequest } from 'xmlhttprequest';


const stationName = [];
const stationLocation = [];
const baseUrl = 'http://apis.data.go.kr/B552584/MsrstnInfoInqireSvc/getMsrstnList';
const params = {
    serviceKey: 'Ss2kvjUY0+0ZLASxYBsNWKfosTc4aweCI5ikb/o9Xkg4r6Uq2N0d3mArOsNawHWoxRfVH+0IOkL89ORQPfMPnw==',
    returnType: 'json',
    numOfRows: '1000',
    pageNo: '1',
    addr: '',
    stationName: ''
}

const serialize = (obj) => {
    const str = [];
    for (let p in obj) {
        if (obj.hasOwnProperty(p)) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        }
    }
    return str.join("&");
};

const makeRequest = (method, url) => {
    return new Promise((resolve, reject) => {
        let xhr = new XMLHttpRequest();
        xhr.open(method, url);
        xhr.onreadystatechange = function () {
            if (this.readyState == 4) {
                if (this.status == 200) {
                    resolve(xhr);
                }
                else {
                    reject(xhr);
                }
            }
        }
        xhr.send();
    });
};


const urls = [];

fs.createReadStream('station_list.csv')
.pipe(csv({headers: false}))
.on('data', (data) => {
    params.addr = data[0];
    params.stationName = data[1];
    let url = baseUrl + '?' + serialize(params);

    urls.push(url);
})
.on('end', () => {
    console.log("csv completed");

    (async () => {
        const results = [];
        const failReqests = [];

        for (let i = 0; i < urls.length; i++) {
            const url = urls[i];
            try {
                // const result = await makeRequest('GET', url);
                // results.push(result);

                await new Promise(resolve => setTimeout(resolve, 3000));
                console.log('fetch');

                const response = await fetch(url);
                if (response.ok) {
                    const json = await response.json();
                    const items = json.response.body.items;
                    console.log(items);

                    items.forEach(item => {
                        results.push(item.stationName + ' ' + item.dmX + ' ' +  item.dmY);  
                        console.log(item.stationName + ' ' + item.dmX + ' ' +  item.dmY);
                    });
                }
                else {
                    console.log('fail1');
                    console.log(url);
                    console.log(response);
                    failReqests.push(url);
                }
            }
            catch (err) {
                console.log('fail2');
                console.log(err);
                console.log(url);
                failReqests.push(url);
            }
        }

        if (results.length > 0) {
            console.log('----------------------------');
            console.log('results');
            fs.writeFile('stationLocation.txt', results.join('\n'), (err) => console.log(err));
        }
        if (failReqests.length > 0) {
            console.log('----------------------------');
            console.log('failRequests');
            fs.writeFile('failRequests.txt', failReqests.join('\n'), (err) => console.log(err));
        }
    })();
});



