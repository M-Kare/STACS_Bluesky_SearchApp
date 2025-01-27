<script setup lang="ts">
  import Post from "@/components/Post.vue";
import type { PostDTO } from "@/components/PostDTO";
  const props = defineProps<{
    posts: PostDTO[]
  }>()

  function openInNewTab(author:string, uri: string){
    const postKey = uri.split("/").pop()
    window.open(`https://bsky.app/profile/${author}/post/${postKey}`, '_blank')
  }
</script>

<template>
  <ul>
    <li v-for="(post) in posts" :key="post.uri">
      <Post @click="openInNewTab(post.author, post.uri)" :post="post"></Post>
    </li>
  </ul>
</template>

<style scoped>
ul {
  list-style-type: none;
  padding: 0;
}
</style>
