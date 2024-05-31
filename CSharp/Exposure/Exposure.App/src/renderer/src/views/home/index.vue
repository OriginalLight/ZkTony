<template>
  <a-row class="container">
    <a-col :span="14" class="image-preview">
      <ImagePreview :image="preview" @adjust="handlePhotoAdd" />
    </a-col>
    <a-col :span="10" class="operation">
      <CameraOptions @shoot="handleShoot" @preview="handlePreview" />
      <AlbumView
        :preview="preview"
        :albums="albums"
        :selected-albums="selectedAlbums"
        :selected-photos="selectedPhotos"
        :album-preview="albumPreview"
        @click-album="handleClickAlbum"
        @click-photo="handleClickPhoto"
        @combine="handlePhotoAdd"
      />
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import ImagePreview from './components/image-preview.vue'
import CameraOptions from './components/camera-options.vue'
import AlbumView from './components/album-view.vue'
import { Album, Photo } from '@renderer/api/album'
import useHomeState from '@renderer/states/home'

const { preview, selectedAlbums, selectedPhotos, albums, albumPreview } = useHomeState()

// 拍摄
const handleShoot = (album: Album) => {
  if (album.photos.length > 0) {
    preview.value = album.photos.find((item) => item.type === 1) ?? album.photos[0]
  }
  const list: Album[] = []
  albums.value = list.concat(album).concat(albums.value)
  selectedPhotos.value = []
  albumPreview.value = album
}

// 预览
const handlePreview = (image: Photo) => {
  preview.value = image
}

// 点击相册
const handleClickAlbum = (album: Album) => {
  if (album.photos.length > 0) {
    preview.value = album.photos.find((item) => item.type === 1) ?? album.photos[0]
  }
  if (selectedAlbums.value.some((item) => item.id === album.id)) {
    selectedAlbums.value = selectedAlbums.value.filter((item) => item.id !== album.id)
  } else {
    selectedAlbums.value = selectedAlbums.value.concat(album)
  }
  selectedPhotos.value = []
  albumPreview.value = album
}

// 点击照片
const handleClickPhoto = (photo: Photo) => {
  if (selectedPhotos.value.some((item) => item.id === photo.id)) {
    selectedPhotos.value = selectedPhotos.value.filter((item) => item.id !== photo.id)
  } else {
    selectedPhotos.value = selectedPhotos.value.concat(photo)
  }
  preview.value = photo
}

const handlePhotoAdd = (photo: Photo | null) => {
  if (photo) {
    preview.value = photo
    selectedPhotos.value = []
    const album = albums.value.find((item) => item.id === photo.albumId)
    if (album) {
      const photos = album.photos.concat(photo)
      album.photos = photos
      // repace album to update the album list
      albums.value = albums.value.map((item) => (item.id === album.id ? album : item))
      albumPreview.value = album
    }
  }
}
</script>

<style lang="less" scoped>
.container {
  height: calc(100vh - 66px);
  padding: 8px;
  overflow: hidden;

  .image-preview {
    position: relative;
    display: flex;
    flex-direction: column;
    height: calc(100vh - 66px - 16px);
    overflow: hidden;
  }

  .operation {
    display: flex;
    flex-direction: column;
    overflow: hidden;
    height: calc(100vh - 66px - 16px);
    padding-left: 8px;
  }
}
</style>
