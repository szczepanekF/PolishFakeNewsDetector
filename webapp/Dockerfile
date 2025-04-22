# Step 1: Build React app
FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build

# Step 2: Use Nginx to serve the built React app
FROM nginx:stable-alpine

# Copy the build output to Nginx's default directory
COPY --from=build /webapp/build /usr/share/nginx/html

EXPOSE 80

# Nginx will automatically start when the container runs
CMD ["nginx", "-g", "daemon off;"]
