from flask import Flask,request
import csv

with open('imu.csv','w') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(['accx','accy','accz','gyrox','gyroy','gyroz','magx','magy','magz','Timestamp'])
        
app = Flask(__name__)
 
@app.route('/server', methods=['GET','POST'])
def server():
    r = request.form
    data = r.to_dict(flat=False)
    temp = str(data['Timestamp'])
    t = int(temp[2:-3])
    temp = str(data['accx'])
    accx = float(temp[7:-2])
    temp = str(data['accy'])
    accy = float(temp[7:-2])
    temp = str(data['accz'])
    accz = float(temp[7:-2])
    temp = str(data['gyrox'])
    gyrox = float(temp[8:-2])
    temp = str(data['gyroy'])
    gyroy = float(temp[8:-2])
    temp = str(data['gyroz'])
    gyroz = float(temp[8:-2])
    temp = str(data['magx'])
    magx = float(temp[7:-2])
    temp = str(data['magy'])
    magy = float(temp[7:-2])
    temp = str(data['magz'])
    magz = float(temp[7:-2])
    imu_data = [accx,accy,accz,gyrox,gyroy,gyroz,magx,magy,magz,t]
    with open('imu.csv','a+') as csv_file:
        writer = csv.writer(csv_file)
        writer.writerow(imu_data)
    return("ok")
 
if __name__ == '__main__':
    app.run(host='0.0.0.0')
