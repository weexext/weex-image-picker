

const WeexImagePicker = {
  show() {
      alert("module WeexImagePicker is created sucessfully ")
  }
};


var meta = {
   WeexImagePicker: [{
    name: 'show',
    args: []
  }]
};



if(window.Vue) {
  weex.registerModule('WeexImagePicker', WeexImagePicker);
}

function init(weex) {
  weex.registerApiModule('WeexImagePicker', WeexImagePicker, meta);
}
module.exports = {
  init:init
};
