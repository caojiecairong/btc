// const zbInit = require("../js/zb").zbInit;
const read = require("fs");
const pako = require("pako");

(function() {
  var vue = {};
  let icons = ["BTC", "DASH", "ETH", "EOS", "BCH", "ETC", "XRP", "QTUM", "LTC"];
  var platData = {};

  var error = {
    ZB: {
      count: 0,
      maxRetry: 2,
      time: 0,
      intervalId: 0
    }
  };
  function init() {
    initPlatData(); //初始化平台数据
    initVue(); //初始化vue
    initDataRequest(); //初始化数据请求
  }

  function initPlatData() {
    let platform = [
      { plat1: "ZB", plat2: "BITH", size: "1" },
      { plat1: "ZB", plat2: "BITH", size: "5" },
      { plat1: "HB", plat2: "BITH", size: "1" },
      { plat1: "HB", plat2: "BITH", size: "5" }
    ];
    platform.forEach(({ plat1, plat2, size }) => {
      platData[plat1 + "_BUY" + size] = {};
      platData[plat1 + "_SELL" + size] = {};
      icons.forEach(b => {
        let iconBuy = {};
        iconBuy[plat1 + "_BUY"] = (Math.random() * 10000).toFixed(0);
        iconBuy[plat2 + "_SELL"] = (Math.random() * 10000).toFixed(0);
        iconBuy["DIFF"] = Math.random();
        iconBuy["CHANGE"] = false;
        platData[plat1 + "_BUY" + size][b] = iconBuy;
        let iconSell = {};
        iconSell[plat1 + "_SELL"] = (Math.random() * 10000).toFixed(0);
        iconSell[plat2 + "_BUY"] = (Math.random() * 10000).toFixed(0);
        iconSell["DIFF"] = Math.random();
        iconSell["CHANGE"] = false;
        platData[plat1 + "_SELL" + size][b] = iconSell;
      });
    });
    return platData;
  }

  function initDataRequest() {
    ZBHttpReq();
    ZBWebSocket();
    HBWebSocket();
    setInterval(BithumHttp, 3000);
  }
  function ZBWebSocket() {
    console.log(" ZB WebSocket 请求" + new Date().toLocaleTimeString());
    ws = new WebSocket("wss://api.bitkk.com:9999/websocket");
    ws.onopen = () => {
      icons.forEach(o => {
        ws.send(
          JSON.stringify({
            event: "addChannel",
            channel:
              "BCH" === o ? "bccusdt_depth" : o.toLowerCase() + "usdt_depth"
          })
        );
      });
    };
    ws.onmessage = rep => {
      let data = JSON.parse(rep.data);
      let icon = data.channel.split("usdt_depth")[0].toUpperCase();
      icon = "BCC" === icon ? "BCH" : icon;

      ZBSet(icon, data);
    };
    ws.onclose = function() {
      ZBWebSocketError();
    };
    ws.onerror = function() {
      ZBWebSocketError();
    };
  }

  function ZBHttp() {
    if (new Date().getTime() - error.ZB.time > error.ZB.maxRetry * 60 * 1000) {
      console.log("结束 ZB HTTP 请求" + new Date().toLocaleTimeString());
      error.ZB.maxRetry += 1;
      clearInterval(error.ZB.intervalId);
      ZBWebSocket();
    } else {
      ZBHttpReq();
    }
  }
  function ZBHttpReq() {
    icons.forEach(icon => {
      let channel =
        "BCH" === icon ? "bcc_usdt" : icon.toLocaleLowerCase() + "_usdt";
      console.log(
        "请求 ZB Http 更新 " + icon + new Date().toLocaleTimeString()
      );
      ajax(
        "http://api.bitkk.com/data/v1/depth?market=" + channel,
        {},
        rep => {
          ZBSet(icon, JSON.parse(rep));
        },
        err => {}
      );
    });
  }
  function ZBSet(icon, { bids, asks }) {
    console.log("ZB  更新 " + icon + new Date().toLocaleTimeString());
    let asks_short = asks.slice(asks.length - 5);
    vue.$nextTick(function() {
      vue.ZB_BUY1[icon].ZB_BUY = bids[0][0];
      vue.ZB_BUY5[icon].ZB_BUY = bids[4][0];
      vue.ZB_SELL1[icon].ZB_SELL = asks_short[4][0];
      vue.ZB_SELL5[icon].ZB_SELL = asks_short[0][0];
    });
  }

  function ZBWebSocketError(e) {
    error.ZB.count++;
    if (error.ZB.count < error.ZB.maxRetry) {
      error.ZB.time = new Date().getTime();
      console.error("zb WebSocket 无法访问,重试第" + error.ZB.count + "次");
      ZBWebSocket();
    } else {
      console.log("开始 ZB HTTP 请求" + new Date().toLocaleTimeString());
      error.ZB.intervalId = setInterval(ZBHttp, 3000);
    }
  }

  function BithumHttp() {
    console.log("请求   bithumb更新" + new Date().toLocaleTimeString());
    ajax(
      "https://api.bithumb.com/public/orderbook/ALL",
      {},
      rep => {
        console.log("收到   bithumb更新" + new Date().toLocaleTimeString());
        icons.forEach(icon => {
          if ("BCC" === icon) icon = "BCH";
          let data = rep.data[icon];
          vue.$nextTick(function() {
            console.log(
              "bithumb成功更新货币" + icon + new Date().toLocaleTimeString()
            );
            vue.ZB_BUY1[icon].BITH_SELL = data.asks[0].price;
            vue.ZB_BUY5[icon].BITH_SELL = data.asks[4].price;
            vue.ZB_SELL1[icon].BITH_BUY = data.bids[0].price;
            vue.ZB_SELL5[icon].BITH_BUY = data.bids[4].price;
          });
        });
      },
      err => {}
    );
  }

  function HBWebSocket() {
    let ws = new WebSocket("wss://api.huobipro.com/ws");
    ws.onopen = () => {
      icons.forEach(icon => {
        let symbol = icon.toLocaleLowerCase() + "usdt";
        ws.send(
          JSON.stringify({
            sub: `market.${symbol}.depth.step0`,
            id: `${icon}`
          })
        );
      });
    };
    ws.onmessage = rep => {
      let promise = new Promise(function(resolve, reject) {
        let arrayBufferNew = "";
        var reader = new FileReader();
        reader.onload = function(pro) {
          arrayBufferNew = this.result;
          resolve(
            pako.inflate(arrayBufferNew, {
              to: "string"
            })
          );
        };
        reader.readAsArrayBuffer(rep.data);
      });
      promise.then(rep => {
        let data = JSON.parse(rep);
        if (data.ping) {
          ws.send(JSON.stringify({ pong: data.ping }));
        } else if (data.tick) {
          let icon = data.ch
            .split(".")[1]
            .split("usdt")[0]
            .toLocaleUpperCase();
          HBSet(icon, data.tick);
        } else {
          console.log(new Date().toLocaleTimeString(), rep);
        }
      });
    };
    ws.onclose = () => {
      HBWebSocket();
    };
    ws.onerror = rep => {
      console.log("HB WebSocket error: ", rep);
      HBWebSocket();
    };
  }
  function HBSet(icon, { bids, asks }) {
    console.log("HB  更新 " + icon + new Date().toLocaleTimeString());
    vue.$nextTick(function() {
      vue.HB_BUY1[icon].HB_BUY = bids[0][0];
      vue.HB_BUY5[icon].HB_BUY = bids[4][0];
      vue.HB_SELL1[icon].HB_SELL = asks[0][0];
      vue.HB_SELL5[icon].HB_SELL = asks[4][0];
    });
  }
  function initVue() {
    vue = new Vue({
      el: "#vue-container",
      data() {
        let data = platData;
        data.displayAll = "";
        return data;
      },
      methods: {
        diff(bit, zb) {
          return (bit - zb * 1070) / bit;
        },
        change(obj, key1, key2) {
          let list = Object.entries(obj);
          let self = this;
          list.forEach(o => {
            let oldDiff = o[1].DIFF;
            o[1].DIFF = self.diff(o[1][key1], o[1][key2]);
            obj[o[0]].DIFF = o[1].DIFF;
            o[1].CHANGE = o[1].DIFF - oldDiff >= 0;
          });
          return list;
        },
        sort(obj, key1, key2, order) {
          let list = this.change(obj, key1, key2);
          list.sort((a, b) => {
            if ("asc" === order) {
              return a[1].DIFF - b[1].DIFF;
            }
            return b[1].DIFF - a[1].DIFF;
          });
          return list;
        }
      },
      computed: {
        ZB_SELL1_List: function() {
          return this.sort(this.ZB_SELL1, "BITH_BUY", "ZB_SELL");
        },
        ZB_BUY1_List: function() {
          return this.sort(this.ZB_BUY1, "BITH_SELL", "ZB_BUY", "asc");
        },
        ZB_SELL5_List: function() {
          return this.sort(this.ZB_SELL5, "BITH_BUY", "ZB_SELL");
        },
        ZB_BUY5_List: function() {
          return this.sort(this.ZB_BUY5, "BITH_SELL", "ZB_BUY", "asc");
        },
        HB_SELL1_List: function() {
          return this.sort(this.HB_SELL1, "BITH_BUY", "HB_SELL");
        },
        HB_BUY1_List: function() {
          return this.sort(this.HB_BUY1, "BITH_SELL", "HB_BUY", "asc");
        },
        HB_SELL5_List: function() {
          return this.sort(this.HB_SELL5, "BITH_BUY", "HB_SELL");
        },
        HB_BUY5_List: function() {
          return this.sort(this.HB_BUY5, "BITH_SELL", "HB_BUY", "asc");
        }
      },
      mounted() {}
    });
  }

  function ajax(url, data, successCallback, errorCallback) {
    return $.ajax({
      type: "GET",
      url: url,
      data: data,
      success: function(data) {
        successCallback(data);
      }
    }).fail(function(response) {
      errorCallback(response);
    });
  }

  init();
})();
