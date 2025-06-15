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

COPY .env .
COPY . .
RUN npm run build

# Step 2: Use Nginx to serve the built React app
FROM nginx:stable-alpine

# Copy the build output to Nginx's default directory
COPY --from=build /app/build /usr/share/nginx/html

EXPOSE 80

# Nginx will automatically start when the container runs
CMD ["nginx", "-g", "daemon off;"]
