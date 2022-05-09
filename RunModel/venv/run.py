import tensorflow as tf
from tensorflow import keras
from keras.preprocessing import image
import numpy as np
import os

model = tf.keras.models.load_model('C:\\Users\\Batur\\HSE\\digitClassification\\RunModel\\venv\\mnist.h5')

model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])

img = image.load_img('C:\\Users\\Batur\\HSE\\digitClassificationFolder\\digit.jpg', grayscale=True)
# x = image.img_to_array(img)
x = np.expand_dims(img, axis=0)
prediction = model.predict(x)

path = os.path.join("C:\\Users\\Batur\\HSE\\digitClassificationFolder", "answer")
os.mkdir(path)

answFile = open("C:\\Users\\Batur\\HSE\\digitClassificationFolder\\answer\\answer.txt", "w")
answFile.write(str(np.argmax(prediction, axis=1)[0]))
answFile.close()
