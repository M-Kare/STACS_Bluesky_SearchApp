<script setup lang="ts">
import {ref} from "vue";

const content = ref("")
const geminiResponse = ref("")

async function sendPrompt(){
  const response = await fetch("/api/gemini/prompt", {
    method: "POST",
    body: content.value
  })
  geminiResponse.value = await response.json()
}

</script>

<template>
  <div>
    <textarea v-model="content" placeholder="What's on your mind?" rows="5"/>
    <input @click="sendPrompt()" type="submit"/>
  </div>
  <div>
    <p>{{ geminiResponse }}</p>
  </div>
</template>

<style scoped>
</style>
