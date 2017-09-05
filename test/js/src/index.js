

const Test = {
  show() {
      alert("module Test is created sucessfully ")
  }
};


var meta = {
   Test: [{
    name: 'show',
    args: []
  }]
};



if(window.Vue) {
  weex.registerModule('Test', Test);
}

function init(weex) {
  weex.registerApiModule('Test', Test, meta);
}
module.exports = {
  init:init
};
