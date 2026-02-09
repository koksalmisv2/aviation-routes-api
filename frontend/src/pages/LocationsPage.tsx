import React, { useEffect, useState } from 'react';
import {
  Table,
  Button,
  Modal,
  Form,
  Input,
  Space,
  Typography,
  message,
  Popconfirm,
  Tag,
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons';
import api from '../api/axiosInstance';
import type { LocationDTO } from '../types';

const { Title } = Typography;

const LocationsPage: React.FC = () => {
  const [locations, setLocations] = useState<LocationDTO[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingLocation, setEditingLocation] = useState<LocationDTO | null>(null);
  const [form] = Form.useForm();

  const fetchLocations = async () => {
    setLoading(true);
    try {
      const { data } = await api.get<LocationDTO[]>('/locations');
      setLocations(data);
    } catch {
      message.error('Failed to load locations');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLocations();
  }, []);

  const openCreateModal = () => {
    setEditingLocation(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEditModal = (record: LocationDTO) => {
    setEditingLocation(record);
    form.setFieldsValue(record);
    setModalOpen(true);
  };

  const handleDelete = async (id: number) => {
    try {
      await api.delete(`/locations/${id}`);
      message.success('Location deleted');
      fetchLocations();
    } catch {
      message.error('Failed to delete location');
    }
  };

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields();
      if (editingLocation?.id) {
        await api.put(`/locations/${editingLocation.id}`, values);
        message.success('Location updated');
      } else {
        await api.post('/locations', values);
        message.success('Location created');
      }
      setModalOpen(false);
      fetchLocations();
    } catch {
      // validation error or API error
    }
  };

  const columns = [
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: (a: LocationDTO, b: LocationDTO) => a.name.localeCompare(b.name),
    },
    {
      title: 'Country',
      dataIndex: 'country',
      key: 'country',
    },
    {
      title: 'City',
      dataIndex: 'city',
      key: 'city',
    },
    {
      title: 'Location Code',
      dataIndex: 'locationCode',
      key: 'locationCode',
      render: (code: string) => <Tag color="blue">{code}</Tag>,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 150,
      render: (_: unknown, record: LocationDTO) => (
        <Space>
          <Button
            type="text"
            icon={<EditOutlined />}
            onClick={() => openEditModal(record)}
          />
          <Popconfirm
            title="Delete this location?"
            description="This action cannot be undone."
            onConfirm={() => handleDelete(record.id!)}
            okText="Delete"
            okButtonProps={{ danger: true }}
          >
            <Button type="text" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Title level={4} style={{ margin: 0 }}>
          Locations
        </Title>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
          Add Location
        </Button>
      </div>

      <Table
        columns={columns}
        dataSource={locations}
        rowKey="id"
        loading={loading}
        pagination={{ pageSize: 10 }}
      />

      <Modal
        title={editingLocation ? 'Edit Location' : 'Add Location'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText={editingLocation ? 'Update' : 'Create'}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item
            name="name"
            label="Name"
            rules={[{ required: true, message: 'Name is required' }]}
          >
            <Input placeholder="e.g. Istanbul Airport" />
          </Form.Item>
          <Form.Item
            name="country"
            label="Country"
            rules={[{ required: true, message: 'Country is required' }]}
          >
            <Input placeholder="e.g. Turkey" />
          </Form.Item>
          <Form.Item
            name="city"
            label="City"
            rules={[{ required: true, message: 'City is required' }]}
          >
            <Input placeholder="e.g. Istanbul" />
          </Form.Item>
          <Form.Item
            name="locationCode"
            label="Location Code"
            rules={[
              { required: true, message: 'Location code is required' },
              { min: 3, message: 'Must be at least 3 characters' },
            ]}
          >
            <Input placeholder="e.g. IST" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
};

export default LocationsPage;
