// { "framework": "Vue" }

/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};

/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {

/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId])
/******/ 			return installedModules[moduleId].exports;

/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			exports: {},
/******/ 			id: moduleId,
/******/ 			loaded: false
/******/ 		};

/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);

/******/ 		// Flag the module as loaded
/******/ 		module.loaded = true;

/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}


/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;

/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;

/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";

/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(0);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

	var __vue_exports__, __vue_options__
	var __vue_styles__ = []

	/* styles */
	__vue_styles__.push(__webpack_require__(1)
	)

	/* script */
	__vue_exports__ = __webpack_require__(2)

	/* template */
	var __vue_template__ = __webpack_require__(3)
	__vue_options__ = __vue_exports__ = __vue_exports__ || {}
	if (
	  typeof __vue_exports__.default === "object" ||
	  typeof __vue_exports__.default === "function"
	) {
	if (Object.keys(__vue_exports__).some(function (key) { return key !== "default" && key !== "__esModule" })) {console.error("named exports are not supported in *.vue files.")}
	__vue_options__ = __vue_exports__ = __vue_exports__.default
	}
	if (typeof __vue_options__ === "function") {
	  __vue_options__ = __vue_options__.options
	}
	__vue_options__.__file = "/Users/dzc/que/ucar/weexext/weex-image-picker/examples/index.vue"
	__vue_options__.render = __vue_template__.render
	__vue_options__.staticRenderFns = __vue_template__.staticRenderFns
	__vue_options__._scopeId = "data-v-724b7d94"
	__vue_options__.style = __vue_options__.style || {}
	__vue_styles__.forEach(function (module) {
	  for (var name in module) {
	    __vue_options__.style[name] = module[name]
	  }
	})
	if (typeof __register_static_styles__ === "function") {
	  __register_static_styles__(__vue_options__._scopeId, __vue_styles__)
	}

	module.exports = __vue_exports__
	module.exports.el = 'true'
	new Vue(module.exports)


/***/ }),
/* 1 */
/***/ (function(module, exports) {

	module.exports = {
	  "container": {
	    "flex": 1
	  }
	}

/***/ }),
/* 2 */
/***/ (function(module, exports) {

	'use strict';

	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//
	//


	var plugin = weex.requireModule('imagePicker');
	module.exports = {
	    data: {
	        value: '',
	        index: 0,
	        txtChange: '',
	        imageUrl: '',
	        res: ""
	    },
	    methods: {
	        createAction: function createAction() {
	            var _this = this;

	            var DEFAULT_OPTIONS = {
	                title: 'Select a Photo',
	                cancelButtonTitle: 'Cancel',
	                takePhotoButtonTitle: 'Take Photo…',
	                chooseFromLibraryButtonTitle: 'Choose from Library…',
	                quality: 1.0,
	                allowsEditing: false,
	                permissionDenied: {
	                    title: 'Permission denied',
	                    text: 'To be able to take pictures with your camera and choose images from your library.',
	                    reTryTitle: 're-try',
	                    okTitle: 'I\'m sure'
	                }
	            };
	            plugin.test();
	            plugin.showImagePicker(DEFAULT_OPTIONS, function (e) {

	                _this.res = JSON.stringify(e);
	                _this.imageUrl = 'file://' + e.path;
	                console.log('imageUrl=' + _this.imageUrl);
	                console.log('res=' + _this.res);
	            });
	        }
	    }
	};

/***/ }),
/* 3 */
/***/ (function(module, exports) {

	module.exports={render:function (){var _vm=this;var _h=_vm.$createElement;var _c=_vm._self._c||_h;
	  return _c('div', {
	    staticClass: ["conatiner"]
	  }, [_c('text', {
	    staticStyle: {
	      marginBottom: "20px"
	    }
	  }, [_vm._v(_vm._s(_vm.res))]), _c('div', {
	    staticStyle: {
	      margin: "20px",
	      padding: "20px",
	      backgroundColor: "#1ba1e2",
	      color: "#fff"
	    },
	    on: {
	      "click": _vm.createAction
	    }
	  }, [_c('text', {
	    staticStyle: {
	      color: "#fff"
	    }
	  }, [_vm._v("选择图片")])]), _c('image', {
	    staticStyle: {
	      height: "300px",
	      width: "300px"
	    },
	    attrs: {
	      "src": _vm.imageUrl
	    }
	  }), _c('image', {
	    staticStyle: {
	      width: "260px",
	      height: "260px"
	    },
	    attrs: {
	      "src": "content:///media/external/images/media/16313Screenshot_2017-04-19-13-48-06.png"
	    }
	  })])
	},staticRenderFns: []}
	module.exports.render._withStripped = true

/***/ })
/******/ ]);