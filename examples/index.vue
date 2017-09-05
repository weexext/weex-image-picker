<template>
    <div class="conatiner">
        <text style="margin-bottom: 20px;">{{res}}</text>
        <div @click="createAction" style="margin: 20px;padding:20px;background-color:#1ba1e2;color:#fff;">
            <text style="color:#fff">选择图片</text>
        </div>
        <image style="height: 300px ;width: 300px;" :src="imageUrl"></image>
    </div>
</template>

<style>
    .container {
        flex: 1;
    }
</style>

<script>

    const plugin = weex.requireModule('imagePicker');
    module.exports = {
        data: {
            value: '',
            index: 0,
            txtChange: '',
            imageUrl: '',
            res: "",
        },
        methods: {
            createAction: function () {
                const DEFAULT_OPTIONS = {
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
                        okTitle: 'I\'m sure',
                    }
                };
                plugin.test();
                plugin.showImagePicker(DEFAULT_OPTIONS, (e) => {

                    this.res = JSON.stringify(e);
                    this.imageUrl = 'file://'+ e.path;
                    console.log('imageUrl=' + this.imageUrl);
                    console.log('res=' +this.res);
                 })
                ;

            }
        }
    }
</script>
