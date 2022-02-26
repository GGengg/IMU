from flask import Flask,request
import csv

with open('imu.csv','w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['Timestamp','accx','accy','accz','gyrox','gyroy','gyroz','magx','magy','magz'])
        
app = Flask(__name__)
 
@app.route('/server', methods=['GET','POST'])
def server():
    r = request.form
    data = r.to_dict(flat=False)

    t = int(str(data['Timestamp'])[2:-2])

    accx = float(str(data['accx'])[2:-2])
    accy = float(str(data['accy'])[2:-2])
    accz = float(str(data['accz'])[2:-2])

    gyrox = float(str(data['gyrox'])[2:-2])
    gyroy = float(str(data['gyroy'])[2:-2])
    gyroz = float(str(data['gyroz'])[2:-2])

    magx = float(str(data['magx'])[2:-2])
    magy = float(str(data['magy'])[2:-2])
    magz = float(str(data['magz'])[2:-2])

    imu_data = [t,accx,accy,accz,gyrox,gyroy,gyroz,magx,magy,magz]

    with open('imu.csv','a+') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(imu_data)
    return("ok")
 
if __name__ == '__main__':
    app.run(host='0.0.0.0')
