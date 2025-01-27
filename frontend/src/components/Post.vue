<template>
  <div class="post">
    <div class="post-header">
      <span class="author">{{ post.author.split(".")[0] }}</span>
      <span class="timestamp">{{ post.timestamp.replace("T", " | ").replace("Z", "") }}</span>
      <span class="sentiment" :style="{'color':sentimentColor}">Sentiment: {{ post.sentimentscore }}</span>
    </div>
    <div class="post-content">
      <p>{{ post.content }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type {PostDTO} from "@/components/PostDTO.ts";
import {ref} from "vue";

const props = defineProps<{
  post: PostDTO
}>()

const sentimentColor = ref("#777")
if(props.post.sentimentscore >= 3 && props.post.sentimentscore <= 4){
  sentimentColor.value = 'green'
} else if (props.post.sentimentscore > 2 && props.post.sentimentscore < 3){
  sentimentColor.value = 'darkgreen'
} else if(props.post.sentimentscore < 2 && props.post.sentimentscore > 1){
  sentimentColor.value = 'red'
} else if(props.post.sentimentscore <= 1 && props.post.sentimentscore >= 0){
  sentimentColor.value = 'darkred'
}

</script>

<style scoped>
.post {
  padding: 16px;
  border-radius: 8px;
  background-color: #ffffff;
  margin-bottom: 16px;
  color: #1e1e1e;
}

.post:hover {
  background-color: #e1e1e1;
  color: #000000
}

.post-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.sentiment {
  color: #777777;
}

.author {
  font-weight: bold;
  margin-right: 16px;
}

.timestamp {
  font-weight: lighter;
  text-align: right;
  color: #777;
  font-size: 0.9em;
}

.post-content {
  font-size: 1em;
  line-height: 1.6;
}
</style>

