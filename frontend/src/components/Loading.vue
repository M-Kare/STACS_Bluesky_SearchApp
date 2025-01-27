<script setup lang="ts">
</script>

<template>
  <div class="background">
    <div class="box">
      <span id="loadingText">Fetching Posts...</span>
    </div>
  </div>
</template>

<style scoped>
/* Full-screen dark background */
.background {
  position: fixed; /* Position it fixed so it stays on top of everything */
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background-color: rgba(0, 0, 0, 0.6); /* Slightly dark background */
  display: flex;
  justify-content: center;
  align-items: center; /* Centers the box vertically and horizontally */
  z-index: 1000; /* Ensures it is on top of other content */
}

/* White centered box with text */
.box {
  background-color: white;
  padding: 20px;
  width: 500px;
  height: 200px;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 20px;
  font-family: Arial, sans-serif;
  border-radius: 8px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

#loadingText{
  color: #1e1e1e;
}
</style>
