# Step 1: Build React app
FROM node:20-alpine AS build

WORKDIR /app

# Accept build-time arguments
ARG REACT_APP_AUTH_API
ARG REACT_APP_LOGIC_API

# Pass them as environment variables (optional but useful for debugging)
ENV REACT_APP_AUTH_API=$REACT_APP_AUTH_API
ENV REACT_APP_LOGIC_API=$REACT_APP_LOGIC_API

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

# Nginx will automatically start when the container runs
CMD ["npm", "start"]
