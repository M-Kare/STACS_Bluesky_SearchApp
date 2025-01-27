<script setup lang="ts">
  import {ref} from "vue";

  const postContent = ref("")

  async function submitPost() {
    if (!postContent.value.trim()) {
      alert("Please no empty")
      return
    }
    const response = await fetch("/api/user/post", {
      method: "POST",
      body: postContent.value
    })
    if(!response.ok){
      console.error(response.statusText)
      return
    }
    postContent.value = ""
  }
</script>

<template>
  <div class="post-container">
    <h2>Create a Post</h2>
    <textarea v-model="postContent"
      placeholder="What's on your mind?"
      rows = 5
    ></textarea>
  <br>
    <button @click="submitPost()">
      Submit Post
    </button>
  </div>
</template>

<style scoped>
textarea {
  width: 99%;
}

button {
  margin-left: auto;
  margin-right: 0;
  display: block;
  /* Optional: Add padding and margin to space it properly */
}
</style>
